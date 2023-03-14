package ir

import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
import ir.lowered.*
import ir.lowered.LIRExpr.*
import ir.lowered.LIRStmt.*
import ir.mid.*
import ir.mid.IRStmt.IRSeq

class IRLowerer() {
    private var freshLowTempCount = 0
    private fun freshTemp(): LIRTemp {
        freshLowTempCount++
        return LIRTemp("\$TL$freshLowTempCount")
    }

    private fun commutes(op: IRBinOp.OpType): Boolean {
        return when (op) {
            ADD -> true
            SUB -> false
            MUL -> true
            HMUL -> true
            DIV -> false
            MOD -> false
            AND -> false
            OR -> false
            XOR -> true // TODO: Real sus...doesn't exist in eta
            LSHIFT -> false
            RSHIFT -> false
            ARSHIFT -> false
            EQ -> true
            NEQ -> true
            LT -> false
            ULT -> false
            GT -> false
            LEQ -> false
            GEQ -> false
        }
    }

    fun lowirgen(midIR: IRCompUnit, optimize: Boolean = false): LIRCompUnit {
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
                stmts.add(LIRJump(guardExpr))
                stmts
            }

            is IRStmt.IRLabel -> listOf(LIRLabel(n.l))
            is IRStmt.IRMove -> {
                val stmts: MutableList<FlatStmt> = mutableListOf()
                when (n.dest) {
                    is IRExpr.IRTemp -> {
                        val (exprStmts, expr) = lowerExpr(n.expr)

                        stmts.addAll(exprStmts)
                        stmts.add(LIRMove(LIRTemp(n.dest.name), expr))
                    }

                    is IRExpr.IRMem -> {
                        val (e1Stmts, e1) = lowerExpr(n.dest)
                        val (e2Stmts, e2) = lowerExpr(n.expr)
                        val temp = freshTemp()
                        stmts.addAll(e1Stmts)
                        stmts.add(LIRMove(temp, e1))
                        stmts.addAll(e2Stmts)
                        stmts.add(LIRMove(LIRMem(temp), e2))
                    }

                    else -> {
                        throw Exception("moving into non-mem, non-temp expr")
                    }
                }
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
                val addrTmp = freshTemp()
                stmts.addAll(addrStmts)
                stmts.add(LIRMove(addrTmp, addrExpr))

                val argTmps : MutableList<LIRExpr> = mutableListOf()

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

    private fun lowerExpr(n: IRExpr): Pair<List<FlatStmt>, LIRExpr> {
        return when (n) {
            is IRExpr.IRCall -> {
                val allStmts: MutableList<FlatStmt> = mutableListOf()
                val allTemps : MutableList<LIRTemp> = mutableListOf()
                val (s0, e0) = lowerExpr(n.address)
                val t0 = freshTemp()
                allStmts.addAll(s0)
                allStmts.add(LIRMove(t0, e0))

                n.args.forEach {
                    val (si, ei) = lowerExpr(it)
                    val ti = freshTemp()
                    allStmts.addAll(si)
                    allTemps.add(ti)
                    allStmts.add(LIRMove(ti, ei))
                }

                allStmts.add(LIRCallStmt(t0, 1, allTemps))
                val returnVal = freshTemp()
                allStmts.add(LIRMove(returnVal, LIRName("_RV1")))
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

                if (commutes(n.op)) {
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