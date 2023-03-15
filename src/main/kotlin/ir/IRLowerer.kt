package ir

import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
import edu.cornell.cs.cs4120.etac.ir.IRConst
import edu.cornell.cs.cs4120.etac.ir.IRNode
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

    private fun commutes(left : IRExpr, right :IRExpr): Boolean {
        //TODO: add commuting
        return false
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
                //TODO: add commuting
                val stmts: MutableList<FlatStmt> = mutableListOf()
                val (e1Stmts, e1) = lowerExpr(n.dest)
                val (e2Stmts, e2) = lowerExpr(n.expr)

                if (commutes(n.dest, n.expr)){
                    stmts.addAll(e1Stmts)
                    stmts.addAll(e2Stmts)
                    stmts.add(LIRMove(e1, e2))
                } else {
                    when (n.dest) {
                        is IRExpr.IRTemp -> {
                            stmts.addAll(e2Stmts)
                            stmts.add(LIRMove(LIRTemp(n.dest.name), e2))
                        }
                        is IRExpr.IRMem -> {
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
                stmts.addAll(addrStmts)

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

                // constant folding
                if (leftExpr is LIRConst && rightExpr is LIRConst) {
                    allStmts.addAll(leftStmt)
                    allStmts.addAll(rightStmt)
                    Pair(allStmts, LIRConst(calculate(leftExpr.value, rightExpr.value, n.op)))
                }
                else {
                    if (commutes(n.left, n.right)){
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
            }

            is IRExpr.IRTemp -> Pair(listOf(), LIRTemp(n.name))
        }
    }

    private fun calculate(n1 : Long, n2 : Long, op : IRBinOp.OpType) : Long {
        return when (op) {
            ADD -> n1 + n2
            SUB -> n1 - n2
            MUL -> lowMul(n1, n2)
            HMUL -> highMul(n1, n2)
            DIV -> n1 / n2
            MOD -> n1 % n2
            AND -> n1 and n2
            OR -> n1 or n2
            XOR -> n1 xor n2
            LSHIFT -> n1 shl n2.toInt()
            RSHIFT -> n1 ushr n2.toInt()
            ARSHIFT -> n1 shr n2.toInt()
            EQ -> if (n1 == n2) 1 else 0
            NEQ -> if (n1 != n2) 1 else 0
            LT -> if (n1 < n2) 1 else 0
            ULT -> if (n1.toULong() < n2.toULong()) 1 else 0
            GT -> if (n1 > n2) 1 else 0
            LEQ -> if (n1 <= n2) 1 else 0
            GEQ -> if (n1 >= n2) 1 else 0
        }
    }

    private fun lowMul(n1 : Long, n2: Long) : Long {
        val sgn1 = if (n1 >= 0) 1 else -1
        val sgn2 = if (n2 >= 0) 1 else -1
        val n1H = (n1 * sgn1) ushr 32
        val n1L = ((n1 * sgn1) shl 32) ushr 32
        val n2H = (n2 * sgn2) ushr 32
        val n2L = ((n2 * sgn2) shl 32) ushr 32
        return ((n1L * n2L) + ((n1H * n2L) shl 32) + ((n2H * n1L) shl 32)) * sgn1 * sgn2
    }

    private fun highMul(n1 : Long, n2: Long) : Long {
        // https://stackoverflow.com/questions/28868367/getting-the-high-part-of-64-bit-integer-multiplication
        val sgn1 = if (n1 >= 0) 1 else -1
        val sgn2 = if (n2 >= 0) 1 else -1
        val n1H = (n1 * sgn1) ushr 32
        val n1L = ((n1 * sgn1) shl 32) ushr 32
        val n2H = (n2 * sgn2) ushr 32
        val n2L = ((n2 * sgn2) shl 32) ushr 32
        val carry = ((((n1H * n2L) shl 32) ushr 32) + (((n2H * n1L) shl 32) ushr 32) + ((n1L * n2L) ushr 32)) ushr 32
        return ((n1H * n2H) + ((n1H * n2L) ushr 32) + ((n2H * n1L) ushr 32) + carry) * sgn1 * sgn2
    }


}