package ir

import ast.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
import ir.mid.*
import ir.mid.IRExpr.*
import ir.mid.IRStmt.*
import typechecker.EtaType
import edu.cornell.cs.cs4120.etac.ir.IRNode as JIRNode

class IRTranslator(val AST: Program, val name:String) {
    fun irgen(optimize: Boolean = false): JIRNode {
        return translateCompUnit(AST).java
    }

    private fun translateCompUnit(p: Program): IRCompUnit {
        val globals: MutableList<IRData> = ArrayList()
        val functions: MutableList<IRFuncDecl> = ArrayList()

        p.definitions.forEach {
            when (it) {
                is GlobalDecl -> globals.add(translateData(it))
                is Method -> if (it.body != null) functions.add(translateFuncDecl(it))
            }
        }
        return IRCompUnit(name, functions, globals)
    }

    private fun translateData(n: GlobalDecl): IRData {
        val data: LongArray = when (val v = n.value) {
            is Literal.ArrayLit -> v.list.map { translateExpr(it).java.constant() }
                .toLongArray() //let's hope this doesn't throw
            is Literal.BoolLit -> longArrayOf(if (v.bool) 1 else 0)
            is Literal.CharLit -> longArrayOf(v.char.toLong())
            is Literal.IntLit -> longArrayOf(v.num)
            is Literal.StringLit -> v.text.codePoints().asLongStream().toArray()
            null -> "CHARLES <3".codePoints().asLongStream().toArray()
        }
        return IRData(n.id, data)
    }

    private fun translateFuncDecl(n: Method): IRFuncDecl {
        return IRFuncDecl(n.id, translateStatement(n.body!!)) //WOOHOOOOOOO lol
    }

    private fun translateStatement(n: Statement): IRStmt {
        return when (n) {
            is Statement.ArrayInit -> {
                TODO()
            }

            is Statement.Block -> {
                IRSeq(n.stmts.map { translateStatement(it) })
            }

            is Statement.If -> { // TODO: use control translate
                val trueLabel = freshLabel()
                val falseLabel = freshLabel()
                val sequence = mutableListOf(
                    IRCJump(translateExpr(n.guard), trueLabel, falseLabel),
                    trueLabel,
                    translateStatement(n.thenBlock),
                    falseLabel
                )
                if (n.elseBlock != null) sequence.add(translateStatement(n.elseBlock))
                IRSeq(sequence)
            }

            is MultiAssign -> TODO()
            is Statement.Procedure -> {
//                println(mangleMethodName(n.id, n.etaType))
                IRExp(IRCall(IRName(mangleMethodName(n.id, n.etaType)), n.args.map { translateExpr(it) }))
            }

            is Statement.Return -> IRReturn(n.args.map { translateExpr(it) })
            is VarDecl.InitArr -> TODO()
            is VarDecl.RawVarDecl -> IRMove(IRTemp(n.id), IRConst(0)) //INIT 0
            is Statement.While -> {
                val trueLabel = freshLabel()
                val falseLabel = freshLabel()
                val startLabel = freshLabel()
                IRSeq(
                    listOf(
                        startLabel,
                        translateControl(n.guard, trueLabel, falseLabel),
                        trueLabel,
                        translateStatement(n.body),
                        IRJump(IRName(startLabel.l)),
                        falseLabel
                    )
                )
            }
        }
    }

    private fun translateExpr(n: Expr): IRExpr {
        return when (n) {
            is Expr.ArrayAccess -> {
                val tempA = IRTemp("a")
                val tempI = IRTemp("i")
                val successLabel = freshLabel()
                IRESeq(
                    IRSeq(
                        listOf(
                            IRMove(tempA, translateExpr(n.arr)), IRMove(tempI, translateExpr(n.idx)), IRCJump(
                                IROp(ULT, tempI, IRMem(IROp(SUB, tempA, IRConst(8)))),
                                successLabel,
                                IRLabel("_eta_out_of_bounds")
                            ), // TODO: label must go to function
                            successLabel
                        )
                    ), IRMem(IROp(ADD, tempA, IROp(MUL, tempI, IRConst(8))))
                )
            }

            is BinaryOp -> {
                val opType = when (n.op) {
                    BinaryOp.Operation.PLUS -> ADD
                    BinaryOp.Operation.MINUS -> SUB
                    BinaryOp.Operation.TIMES -> MUL
                    BinaryOp.Operation.HIGHTIMES -> HMUL
                    BinaryOp.Operation.DIVIDE -> DIV
                    BinaryOp.Operation.MODULO -> MOD
                    BinaryOp.Operation.LT -> LT
                    BinaryOp.Operation.LEQ -> LEQ
                    BinaryOp.Operation.GT -> GT
                    BinaryOp.Operation.GEQ -> GEQ
                    BinaryOp.Operation.EQB -> EQ
                    BinaryOp.Operation.NEQB -> NEQ
                    BinaryOp.Operation.AND -> AND // TODO: use control translate
                    BinaryOp.Operation.OR -> OR
                }
                IROp(opType, translateExpr(n.left), translateExpr(n.right))
            }

            is Expr.FunctionCall -> IRCall(IRName(mangleMethodName(n.fn, n.etaType)),
                n.args.map { translateExpr(it) })

            is Expr.Identifier -> IRTemp(n.name)
            is Expr.FunctionCall.LengthFn -> IRCall(IRName("_Ilength_iai"), listOf(translateExpr(n.arg)))
            is Literal.ArrayLit -> { // TODO: deal with nested arrays https://edstem.org/us/courses/34931/discussion/2754450
                val tempN = IRTemp("n")
                val tempM = IRTemp("m")

                val moves = mutableListOf(
                    IRMove(tempN, IRCall(IRName("malloc"), listOf(IRConst((n.list.size * 8 + 8).toLong())))),
                    IRMove(IRMem(tempM), IRConst(n.list.size.toLong())),
                )
                for (i in 0 until n.list.size) {
                    moves.add(IRMove(IRMem(IROp(ADD, tempM, IRConst((8 * i).toLong()))), translateExpr(n.list[i])))
                }

                IRESeq(
                    IRSeq(
                        moves
                    ), IROp(ADD, tempM, IRConst(8))
                )
            }

            is Literal.BoolLit -> IRConst(if (n.bool) 1 else 0)
            is Literal.CharLit -> IRConst(n.char.toLong())
            is Literal.IntLit -> IRConst(n.num)
            is Literal.StringLit -> IRConst(0) //TODO LOL
            is UnaryOp -> when (n.op) {
                UnaryOp.Operation.NOT -> IROp(XOR, IRConst(1), translateExpr(n.arg))
                UnaryOp.Operation.NEG -> IROp(SUB, IRConst(0), translateExpr(n.arg))
            }
        }
    }

    fun translateControl(n: Expr, trueLabel: IRLabel, falseLabel: IRLabel): IRStmt {
        return when (n) {
            is Literal.BoolLit -> if (n.bool) IRJump(IRName(trueLabel.l)) else IRJump(IRName(falseLabel.l))
            else -> IRCJump(translateExpr(n), trueLabel, falseLabel)
        }
    }

//    fun optimize(ir: IRCompUnit): IRCompUnit? {
//        return null;
//    }
//
//    fun lower(ir: IRCompUnit): Unit {
//
//    }
//
//    fun literally(v: Literal): LongArray {
//        when (v) {
//            is Literal.ArrayLit -> v.list.map { translateExpr(it).java.constant() }
//                .toLongArray()
//
//            is Literal.BoolLit -> longArrayOf(if (v.bool) 1 else 0)
//            is Literal.CharLit -> longArrayOf(v.char.toLong())
//            is Literal.IntLit -> longArrayOf(v.num)
//            is Literal.StringLit -> v.text.codePoints().asLongStream().toArray()
//        }
//    }
}