package ir

import ir.lowered.*
import ir.lowered.LIRExpr.*
import ir.lowered.LIRStmt.*
import ir.mid.IRCompUnit
import ir.mid.IRExpr
import ir.mid.IRFuncDecl
import ir.mid.IRStmt
import ir.mid.IRStmt.IRSeq

class IRLowerer(val globals: List<String>) {
    private var freshLowTempCount = 0
    private var opt = false
    private var globalsByFunction: MutableMap<String, MutableList<String>> = mutableMapOf()

    private fun freshTemp(): LIRTemp {
        freshLowTempCount++
        return LIRTemp("\$TL$freshLowTempCount")
    }

    private fun isGlobal(name: String): Boolean {
        return globals.contains(name)
    }

    /** Checks if the statements in stmts changes the value of expr */
    private fun commutes(stmts: List<FlatStmt>, expr: LIRExpr): Boolean {
        var memUsed = false
        var unknownGlobalsUsed = false
        var unknownTempsUsed = false
        val tempsUsed = mutableSetOf<String>()
        val globalsUsed = mutableSetOf<String>()

        fun updateMemTempsUsed(node: FlatStmt) {
            when (node) {
                is LIRCallStmt -> {
                    //TODO Optionally track which variables are touched during a call
                    unknownGlobalsUsed = true
                    memUsed = true

//                    unknownTempsUsed = true
//                    when (val calledfn = node.args.first()){
//                        is LIRName -> {
//                            globalsByFunction[calledfn.l]?.forEach { globalsUsed.add(it) }
//                        } else -> {
//                            unknownGlobalsUsed = true
//                            memUsed = true
//                        }
//                    }
                }

                is LIRLabel -> {}
                is LIRMove -> {
                    when (node.dest) {
                        is LIRMem -> memUsed = true
                        is LIRTemp -> tempsUsed.add(node.dest.name)
                        else -> {
                            throw Exception("Invalid LIRMove")
                        }
                    }
                }

                else -> {
                    unknownGlobalsUsed = true
                    memUsed = true
                    unknownTempsUsed = true
                }
            }

        }

        stmts.forEach() {
            updateMemTempsUsed(it)
        }

        fun exprCommutes(expr: LIRExpr): Boolean {
            return when (expr) {
                is LIRConst -> true
                is LIRMem -> !memUsed
                is LIRName -> !isGlobal(expr.l) || (!unknownGlobalsUsed && !globalsUsed.contains(expr.l))
                is LIROp -> {
                    exprCommutes(expr.left) && exprCommutes(expr.right)
                }

                is LIRTemp -> !unknownTempsUsed && !tempsUsed.contains(expr.name)
            }
        }
        return exprCommutes(expr)
    }

    fun lowirgen(midIR: IRCompUnit, optimize: Boolean = false): LIRCompUnit {
        opt = optimize
        return lowerCompUnit(midIR)
    }

    private fun lowerCompUnit(n: IRCompUnit): LIRCompUnit {
        return LIRCompUnit(n.name, n.functions.map { lowerFuncDecl(it) }, n.globals)
    }

    private fun lowerFuncDecl(n: IRFuncDecl): LIRFuncDecl {
        return LIRFuncDecl(n.name, LIRSeq(lowerStatement(n.body)))
    }

    private fun lowerStatement(n: IRStmt): List<FlatStmt> {
        return when (n) {
            is IRStmt.IRCJump -> {
                val (guardStmts, guardExpr) = lowerExpr(n.guard)
                val stmts: MutableList<FlatStmt> = mutableListOf()
                stmts.addAll(guardStmts)
                // could translate the labels shrug but why bother
                if (n.falseBranch != null)
                    stmts.add(LIRCJump(guardExpr, LIRLabel(n.trueBranch.l), LIRLabel(n.falseBranch.l)))
                else
                    stmts.add(LIRTrueJump(guardExpr, LIRLabel(n.trueBranch.l)))
                stmts
            }

            is IRStmt.IRExp -> {
                // throw away expression
                lowerExpr(n.expr).first
            }

            is IRStmt.IRJump -> {
                val (addrStmts, guardExpr) = lowerExpr(n.address)
                val stmts: MutableList<FlatStmt> = mutableListOf()
                stmts.addAll(addrStmts)
                if (guardExpr is LIRName)
                    stmts.add(LIRJump(guardExpr))
                else
                    throw Exception("name metamorphosed into a caterpillar")
                stmts
            }

            is IRStmt.IRLabel -> listOf(LIRLabel(n.l))
            is IRStmt.IRMove -> {
                //TODO: add commuting
                val stmts: MutableList<FlatStmt> = mutableListOf()
                stmts.addAll(factorMoveTarget(n.dest, n.expr))
                stmts
            }

            is IRStmt.IRReturn -> { // went off-script for this one
                val loweredExprStmts = n.valList.map { lowerExpr(it) }
                val loweredExprs = loweredExprStmts.map { it.second }
                val stmts: MutableList<FlatStmt> = mutableListOf()
                // do all of the stmts first, then return the lowered exprs
                // not sure this will do side effects correctly
                loweredExprStmts.forEach { stmts.addAll(it.first) }
                stmts.add(LIRReturn(loweredExprs))
                stmts
            }

            is IRSeq -> {
                val stmts: MutableList<FlatStmt> = mutableListOf()
                n.block.forEach { stmts.addAll(lowerStatement(it)) }
                stmts
            }

            is IRStmt.IRCallStmt -> {
                val stmts: MutableList<FlatStmt> = mutableListOf()
                val (addrStmts, addrExpr) = lowerExpr(n.address)
                if (addrExpr !is LIRName) {
                    throw Exception("a LIRName metamorphosed into a beautiful bug from lowerStatement")
                }
                stmts.addAll(addrStmts)

                val argTmps: MutableList<LIRExpr> = mutableListOf()

                n.args.forEach {
                    val (si, ei) = lowerExpr(it)
                    stmts.addAll(si)
                    val ti = freshTemp()
                    argTmps.add(ti)
                    stmts.add(LIRMove(ti, ei))

                }
                stmts.add(LIRCallStmt(addrExpr, n.n_returns, argTmps))
                stmts
            }
        }
    }

    private fun factorMoveTarget(target: IRExpr, arg: IRExpr): List<FlatStmt> {
        val returnList = mutableListOf<FlatStmt>()
        when (target) {
            is IRExpr.IRTemp -> {
                val (e2Stmts, e2) = lowerExpr(arg)
                returnList.addAll(e2Stmts)
                returnList.add(LIRMove(LIRTemp(target.name), e2))
                return returnList
            }

            is IRExpr.IRMem -> {
                val (e1Stmts, e1) = lowerExpr(target.address)
                val (e2Stmts, e2) = lowerExpr(arg)
                if (commutes(e2Stmts, e1)) {
                    returnList.addAll(e1Stmts)
                    returnList.addAll(e2Stmts)
                    returnList.add(LIRMove(LIRMem(e1), e2))
                } else {
                    val temp = freshTemp()
                    returnList.addAll(e1Stmts)
                    returnList.add(LIRMove(temp, e1))
                    returnList.addAll(e2Stmts)
                    returnList.add(LIRMove(LIRMem(temp), e2))
                }
            }

            is IRExpr.IRESeq -> {
                val eseqStmts = lowerStatement(target.statement)
                returnList.addAll(eseqStmts)
                returnList.addAll(factorMoveTarget(target.value, arg))
            }

            else -> {
                throw Exception("moving into non-mem, non-temp expr")
            }
        }
        return returnList
    }

    private fun lowerExpr(n: IRExpr): Pair<List<FlatStmt>, LIRExpr> {
        return when (n) {
            is IRExpr.IRCall -> {
                val allStmts: MutableList<FlatStmt> = mutableListOf()
                val allTemps: MutableList<LIRTemp> = mutableListOf()
                val (s0, e0) = lowerExpr(n.address)
                if (e0 !is LIRName) {
                    throw Exception("a name has metamorphosed into a beautiful bug from lowerExpr")
                }
                allStmts.addAll(s0)

                n.args.forEach {
                    val (si, ei) = lowerExpr(it)
                    val ti = freshTemp()
                    allStmts.addAll(si)
                    allTemps.add(ti)
                    allStmts.add(LIRMove(ti, ei))
                }

                allStmts.add(LIRCallStmt(e0, 1, allTemps))
                val returnVal = freshTemp()
                allStmts.add(LIRMove(returnVal, LIRTemp("_RV1")))
                Pair(allStmts, returnVal)
            }

            is IRExpr.IRConst -> Pair(listOf(), LIRConst(n.value))
            is IRExpr.IRESeq -> {
                val allStmts: MutableList<FlatStmt> = mutableListOf()
                val stmts = lowerStatement(n.statement)
                val (stmt, expr) = lowerExpr(n.value)
                allStmts.addAll(stmts)
                allStmts.addAll(stmt)
                Pair(allStmts, expr)
            }

            is IRExpr.IRMem -> {
                val (stmts, expr) = lowerExpr(n.address)
                Pair(stmts, LIRMem(expr))
            }

            is IRExpr.IRName -> Pair(listOf(), LIRName(n.l))
            is IRExpr.IROp -> {
                val (leftStmt, leftExpr) = lowerExpr(n.left)
                val (rightStmt, rightExpr) = lowerExpr(n.right)
                val allStmts: MutableList<FlatStmt> = mutableListOf()

                if (commutes(rightStmt, leftExpr)) {
                    allStmts.addAll(leftStmt)
                    allStmts.addAll(rightStmt)
                    Pair(allStmts, LIROp(n.op, leftExpr, rightExpr))
                } else {
                    val newTemp = freshTemp()
                    allStmts.addAll(leftStmt)
                    allStmts.add(LIRMove(newTemp, leftExpr))
                    allStmts.addAll(rightStmt)
                    Pair(allStmts, LIROp(n.op, newTemp, rightExpr))
                }
            }

            is IRExpr.IRTemp -> Pair(listOf(), LIRTemp(n.name))
        }
    }


}