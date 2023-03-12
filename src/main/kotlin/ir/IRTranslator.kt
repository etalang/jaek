package ir

import ast.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
import ir.mid.IRCompUnit
import ir.mid.IRExpr
import ir.mid.IRExpr.*
import ir.mid.IRFuncDecl
import ir.mid.IRStmt
import ir.mid.IRStmt.*
import typechecker.EtaType
import edu.cornell.cs.cs4120.etac.ir.IRNode as JIRNode

class IRTranslator(val AST: Program, val name: String, functions: Map<String, EtaType.ContextType.FunType>) {
    private var functionMap = functions.mapValues { mangleMethodName(it.key, it.value) }
    private var freshLabelCount = 0
    private var freshTempCount = 0

    private fun freshLabel(): IRLabel {
        freshLabelCount++
        return IRLabel("\$L$freshLabelCount")
    }

    private fun freshTemp(): IRTemp {
        freshTempCount++
        return IRTemp("\$T$freshTempCount")
    }

    private fun mangleType(t: EtaType): String {
        return when (t) {
            is EtaType.OrdinaryType.ArrayType -> "a" + mangleType(t.t)
            is EtaType.OrdinaryType.BoolType -> "b"
            is EtaType.OrdinaryType.IntType -> "i"
            else -> "Charles Sherk <3"
        }
    }

    private fun mangleMethodName(name: String, type: EtaType?): String {
        return when (type) {
            is EtaType.ContextType.FunType -> {
                val retType = when (val s = type.codomain.lst.size) {
                    0 -> "p"
                    1 -> mangleType(type.codomain.lst.first())
                    else -> "t" + s.toString() + type.codomain.lst.fold("") { acc, e -> acc + mangleType(e) }
                }

                return "_I" + name.replace(
                    "_", "__"
                ) + "_" + retType + type.domain.lst.fold("") { acc, e -> acc + mangleType(e) }
            }

            else -> {
                "what the fuck"
                // throw Exception("what the")
            }
        }
    }

//    private fun mangleMethodName(method: Method): String {
//        if (method.returnTypes.size > 1) "t"
//        return "_I" + method.id.replace("_", "__") + "_" + // INNOVATIVE USE OF MAP jk
//
//                method.returnTypes.fold("") { acc, e -> acc + mangleType(e) } +//CS 3110
//                method.args.fold("") { acc, e -> acc + mangleType(e.type) }  // KATE + ZAK APPROVED
//
//    }


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
        return if (n.returnTypes.size != 0)
            IRFuncDecl(functionMap[n.id]!!, translateStatement(n.body!!))
        else // if the method is a proc, add empty return
            IRFuncDecl(functionMap[n.id]!!, IRSeq(listOf(translateStatement(n.body!!), IRReturn(listOf()))))
    }

    private fun translateAssignTarget(n: AssignTarget): IRExpr {
        return when (n) {
            is AssignTarget.ArrayAssign -> translateExpr(n.arrayAssign)
            is AssignTarget.DeclAssign -> IRTemp(n.decl.id)
            is AssignTarget.IdAssign -> IRTemp(n.idAssign.name)
            is AssignTarget.Underscore -> freshTemp()
        }

    }

    private fun translateStatement(n: Statement): IRStmt {
        return when (n) {
            is Statement.Block -> {
                IRSeq(n.stmts.map { translateStatement(it) })
            }

            is Statement.If -> {
                val trueLabel = freshLabel()
                val falseLabel = freshLabel()
                val sequence = mutableListOf(
                    translateControl(n.guard, trueLabel, falseLabel),
                    trueLabel,
                    translateStatement(n.thenBlock),
                    falseLabel
                )
                if (n.elseBlock != null) sequence.add(translateStatement(n.elseBlock))
                IRSeq(sequence)
            }

            is MultiAssign -> {
                val targetList: List<IRExpr> = n.targets.map { translateAssignTarget(it) }
                val translatedExprs: List<IRExpr> = n.vals.map { translateExpr(it) }
                val assignList: List<IRStmt> = (targetList zip translatedExprs).map { IRMove(it.first, it.second) }
                IRSeq(assignList)
            }

            is Statement.Procedure -> {
                IRExp(IRCall(IRName(functionMap[n.id]!!), n.args.map { translateExpr(it) }))
            }

            is Statement.Return -> {
//            print("RETURNING....${n.args}")
                IRReturn(n.args.map { translateExpr(it) })
            }

            is VarDecl.InitArr -> {
                val (arrLoc, arrInstrs) = arrayInitHelp(n.arrInit.dimensions.toList().filterNotNull())
                IRSeq(
                    listOf(
                        arrInstrs,
                        IRMove(IRTemp(n.id), arrLoc)
                    )
                )
            }

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

    // precondition -- lst always has at least one element
    private fun arrayInitHelp(lst: List<Expr>): Pair<IRTemp, IRSeq> {
        return if (lst.size <= 1) {
            val tempN = freshTemp()
            val tempM = freshTemp()
            Pair(
                tempM,
                IRSeq(
                    listOf(
                        IRMove(tempN, translateExpr(lst[0])),
                        IRMove(
                            tempM,
                            IRCall(IRName("_eta_alloc"), listOf(IROp(ADD, IROp(MUL, tempN, IRConst(8)), IRConst(8))))
                        ), //IRConst((lstLength * 8 + 8).toLong())))),
                        IRMove(IRMem(tempM), tempN),
                        IRMove(tempM, IROp(ADD, tempM, IRConst(8)))
                    )
                )
            )
        } else {
            val loopLabel = freshLabel()
            val complete = freshLabel()
            val counter = freshTemp()
            val arrSize = freshTemp()
            val memTemp = freshTemp()
            val loop = mutableListOf(
                IRMove(arrSize, translateExpr(lst.first())),
                IRMove(
                    memTemp,
                    IRCall(IRName("_eta_alloc"), listOf(IROp(ADD, IROp(MUL, arrSize, IRConst(8)), IRConst(8))))
                ), //IRConst((lstLength * 8 + 8).toLong())))),
                IRMove(IRMem(memTemp), arrSize),
                IRMove(memTemp, IROp(ADD, memTemp, IRConst(8))),
                IRMove(counter, IRConst(0)),
                loopLabel
            )
            val (subtemp, subarray) = arrayInitHelp(lst.drop(1))
            loop.add(subarray)
            loop.addAll(
                listOf(
                    // the array assignment of subtemp into array hole
                    IRMove(IRMem(IROp(ADD, memTemp, IROp(MUL, counter, IRConst(8)))), subtemp),
                    IRMove(counter, IROp(ADD, counter, IRConst(1))),
                    IRCJump(IROp(GT, counter, arrSize), complete, loopLabel),
                    complete
                )
            )
            //make a for loop that will run the allocation instruction as many times as needed
            Pair(
                memTemp,
                IRSeq(loop)
            )
        }
    }


    private fun translateExpr(n: Expr): IRExpr {
        return when (n) {
            is Expr.ArrayAccess -> {
                val tempA = freshTemp()
                val tempI = freshTemp()
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
                    BinaryOp.Operation.AND -> AND
                    BinaryOp.Operation.OR -> OR
                }
                if (opType == AND) {
                    val tempX = freshTemp()
                    val label1 = freshLabel()
                    val label2 = freshLabel()
                    val labelF = freshLabel()
                    IRESeq(
                        IRSeq(
                            listOf(
                                IRMove(tempX, IRConst(0)),
                                IRCJump(translateExpr(n.left), label1, labelF),
                                label1,
                                IRCJump(translateExpr(n.right), label2, labelF),
                                label2,
                                IRMove(tempX, IRConst(1)),
                                labelF
                            )
                        ),
                        tempX
                    )
                }
                if (opType == OR) {
                    val tempX = freshTemp()
                    val label1 = freshLabel()
                    val label2 = freshLabel()
                    val labelT = freshLabel()
                    IRESeq(
                        IRSeq(
                            listOf(
                                IRMove(tempX, IRConst(1)),
                                IRCJump(translateExpr(n.left), labelT, label1),
                                label1,
                                IRCJump(translateExpr(n.right), labelT, label2),
                                label2,
                                IRMove(tempX, IRConst(0)),
                                labelT
                            )
                        ), tempX
                    )
                } else {
                    IROp(opType, translateExpr(n.left), translateExpr(n.right))
                }
            }

            is Expr.FunctionCall -> IRCall(IRName(functionMap[n.fn]!!),
                n.args.map { translateExpr(it) })

            is Expr.Identifier -> IRTemp(n.name)
            is Expr.FunctionCall.LengthFn -> IRCall(IRName("_Ilength_iai"), listOf(translateExpr(n.arg)))
            is Literal.ArrayLit -> {
                val tempM = freshTemp()
                val moves = arrayInitMoves(IRConst(n.list.size.toLong()), tempM)
                for (i in 0 until n.list.size) {
                    moves.add(
                        IRMove(
                            IRMem(IROp(ADD, tempM, IRConst((8 * (i + 1)).toLong()))),
                            translateExpr(n.list[i])
                        )
                    )
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
            is Literal.StringLit -> {
                val stringPtr = freshTemp()

                val moves = arrayInitMoves(IRConst(n.text.length.toLong()), stringPtr)

                for (i in 0 until n.text.length) {
                    moves.add(
                        IRMove(
                            IRMem(IROp(ADD, stringPtr, IRConst((8 * (i + 1)).toLong()))),
                            IRConst(n.text[i].code.toLong())
                        )
                    )
                }

                return IRESeq(
                    IRSeq(
                        moves
                    ), IROp(ADD, stringPtr, IRConst(8))
                )
            }

            is UnaryOp -> when (n.op) {
                UnaryOp.Operation.NOT -> IROp(XOR, IRConst(1), translateExpr(n.arg))
                UnaryOp.Operation.NEG -> IROp(SUB, IRConst(0), translateExpr(n.arg))
            }
        }
    }

    fun arrayInitMoves(lstLength: IRExpr, ptr: IRTemp): MutableList<IRMove> {
        val moves = mutableListOf(
            IRMove(
                ptr,
                IRCall(IRName("_eta_alloc"), listOf(IROp(ADD, IROp(MUL, lstLength, IRConst(8)), IRConst(8))))
            ), //IRConst((lstLength * 8 + 8).toLong())))),
            IRMove(IRMem(ptr), lstLength)
        )
        return moves
    }

    private fun translateControl(n: Expr, trueLabel: IRLabel, falseLabel: IRLabel): IRStmt {
        return when (n) {
            is Literal.BoolLit -> if (n.bool) IRJump(IRName(trueLabel.l)) else IRJump(IRName(falseLabel.l))
            is UnaryOp -> {
                if (n.op == UnaryOp.Operation.NOT) {
                    translateControl(n, falseLabel, trueLabel)
                } else { //shouldn't typecheck
                    throw Exception("used a non-not unary as a guard, unreachable")
                }
            }

            is BinaryOp -> {
                if (n.op == BinaryOp.Operation.AND) {
                    val label1 = freshLabel()
                    IRSeq(
                        listOf(
                            translateControl(n.left, label1, falseLabel),
                            label1,
                            translateControl(n.right, trueLabel, falseLabel)
                        )
                    )
                } else if (n.op == BinaryOp.Operation.OR) {
                    val label1 = freshLabel()
                    IRSeq(
                        listOf(
                            translateControl(n.left, trueLabel, label1),
                            label1,
                            translateControl(n.right, trueLabel, falseLabel)
                        )
                    )
                } else {
                    IRCJump(translateExpr(n), trueLabel, falseLabel)
                }
            }

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
}