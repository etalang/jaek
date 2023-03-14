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
                // TODO: figure out returns and ARGs
                // After calling a function (i.e., CALL_STMT) you can expect the
                // _RV temps will be populated. In the function, you'll have a RETURN node
                // which contains the expressions to return. This should automatically
                // populate the _RV temps if you're using our interpreter.
                allStmts.add(LIRCallStmt(t0, allTemps))
                val newTemp = freshTemp()
                allStmts.add(LIRMove(newTemp, LIRName("_RV1")))
                Pair(allStmts, newTemp)
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
        val n1H = n1 ushr 32
        val n1L = (n1 shl 32) ushr 32
        val n2H = n2 ushr 32
        val n2L = (n2 shl 32) ushr 32
        return (n1L * n2L) + ((n1H * n2L) shl 32) + ((n2H * n1L) shl 32)
    }

    private fun highMul(n1 : Long, n2: Long) : Long {
        // https://stackoverflow.com/questions/28868367/getting-the-high-part-of-64-bit-integer-multiplication
        val n1H = n1 ushr 32
        val n1L = (n1 shl 32) ushr 32
        val n2H = n2 ushr 32
        val n2L = (n2 shl 32) ushr 32
        val carry = ((((n1H * n2L) shl 32) ushr 32) + (((n2H * n1L) shl 32) ushr 32) + ((n1L * n2L) ushr 32)) ushr 32
        return (n1H * n2H) + ((n1H * n2L) ushr 32) + ((n2H * n1L) ushr 32) + carry
    }


}