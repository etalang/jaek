package ir

import ast.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
import ir.lowered.LIRCompUnit
import ir.mid.IRCompUnit
import ir.mid.IRExpr
import ir.mid.IRExpr.*
import ir.mid.IRFuncDecl
import ir.mid.IRStmt
import ir.mid.IRStmt.*
import ir.optimize.ConstantFolder
import typechecker.EtaFunc
import typechecker.EtaType

class IRTranslator(val AST: Program, val name: String, functionTypes: Map<String, EtaFunc>) {
    private var mangledFunctionNames = functionTypes.mapValues { mangleMethodName(it.key, it.value) }
    private val globals: MutableList<IRData> = ArrayList()

    /** Tracks globals changed by a function call **/
    private val globalsByFunction: MutableMap<String, MutableSet<String>> = HashMap()

    /** Tracks function calls done by a function **/
    private val functionCalls: MutableMap<String, MutableSet<String>> = HashMap()

    private var freshLabelCount = 0
    private var freshTempCount = 0

    /** WHERE IT HAPPENS **/
    fun irgen(optimize: Boolean = false): LIRCompUnit {
        val mir = translateCompUnit(AST)
        getGlobalsTouched()

        var lir = IRLowerer(globals.map { it.name }, globalsByFunction).lowirgen(mir, optimize)
        lir.reorderBlocks()
        lir = ConstantFolder().apply(lir)
        return lir
    }

    private fun getGlobalsTouched() {
        val functions: MutableSet<String> = globalsByFunction.keys

        fun bfs(startFunc: String) {
            val visited: MutableSet<String> = HashSet()

            fun visit(func: String) {
                if (func in visited) return
                visited.add(func)
                globalsByFunction[startFunc]?.union(globalsByFunction[func] ?: emptySet())
                functionCalls[func]?.forEach { visit(it) }
            }
            visit(startFunc)
        }
        functions.forEach() { function ->
            bfs(function)
        }
    }

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

    fun mangleMethodName(name: String, type: EtaType?): String {
        return when (type) {
            is EtaFunc -> {
                val retType = when (val s = type.retCount) {
                    0 -> "p"
                    1 -> mangleType(type.codomain.lst.first())
                    else -> "t" + s.toString() + type.codomain.lst.fold("") { acc, e -> acc + mangleType(e) }
                }

                return "_I" + name.replace(
                    "_", "__"
                ) + "_" + retType + type.domain.lst.fold("") { acc, e -> acc + mangleType(e) }
            }

            else -> {
                "i love cs 4120 ta charles sherk"
            }
        }
    }

    private fun translateCompUnit(p: Program): IRCompUnit {
        val functions: MutableList<IRFuncDecl> = ArrayList()
        // Make two passes
        p.definitions.forEach {
            when (it) {
                is GlobalDecl -> {
                    globals.add(translateData(it))
                }

                else -> {}
            }
        }

//        globals.forEach { println(it.name) }

        p.definitions.forEach {
            when (it) {
                is Method -> if (it.body != null) functions.add(translateFuncDecl(it))
                else -> {}
            }
        }
        return IRCompUnit(name, functions, globals)
    }

    // TODO: Arrays in global data should also have their length? how does this affect pointers to their info?
    private fun translateData(n: GlobalDecl): IRData {
        val data: LongArray = when (val v = n.value) {
            is Literal.ArrayLit -> v.list.map { translateExpr(it, "_globals").java.constant() }
                .toLongArray() //let's hope this doesn't throw
            is Literal.BoolLit -> longArrayOf(if (v.bool) 1 else 0)
            is Literal.CharLit -> longArrayOf(v.char.toLong())
            is Literal.IntLit -> longArrayOf(v.num)
            is Literal.StringLit -> longArrayOf(v.text.length.toLong()) + v.text.codePoints().asLongStream().toArray()
            null -> longArrayOf(0)
        }
        return IRData(n.id, data)
    }

    private fun translateFuncDecl(n: Method): IRFuncDecl {
        val funcMoves: MutableList<IRStmt> = mutableListOf()
        for (i in 0 until n.args.size) {
            funcMoves.add(IRMove(IRTemp(n.args[i].id), IRTemp("_ARG${i + 1}")))
        }

        globalsByFunction[mangledFunctionNames[n.id]!!] = mutableSetOf()
        functionCalls[mangledFunctionNames[n.id]!!] = mutableSetOf()

        funcMoves.add(translateStatement(n.body!!, mangledFunctionNames[n.id]!!))
        if (n.returnTypes.size != 0) {
            return IRFuncDecl(mangledFunctionNames[n.id]!!, IRSeq(funcMoves))
        } else { // if the method is a proc, add empty return
            funcMoves.add(IRReturn(listOf()))
            return IRFuncDecl(mangledFunctionNames[n.id]!!, IRSeq(funcMoves))
        }
    }

    private fun translateAssignTarget(n: AssignTarget, sourceFn: String): IRExpr {
        return when (n) {
            is AssignTarget.ArrayAssign -> translateExpr(n.arrayAssign, sourceFn)
            is AssignTarget.DeclAssign -> IRTemp(n.decl.id)
            is AssignTarget.IdAssign -> IRTemp(n.idAssign.name)
            is AssignTarget.Underscore -> freshTemp()
        }

    }

    private fun translateStatement(n: Statement, sourceFn: String): IRStmt {
        return when (n) {
            is Statement.Block -> {
                IRSeq(n.stmts.map { translateStatement(it, sourceFn) })
            }

            is Statement.If -> {
                val trueLabel = freshLabel()
                val falseLabel = freshLabel()
                val endLabel = freshLabel()
                val sequence = mutableListOf(
                    translateControl(n.guard, trueLabel, falseLabel, sourceFn),
                    trueLabel,
                    translateStatement(n.thenBlock, sourceFn),
                    IRJump(IRName(endLabel.l)),
                    falseLabel
                )
                if (n.elseBlock != null) {
                    sequence.add(translateStatement(n.elseBlock, sourceFn))
                }
                sequence.add(endLabel)
                IRSeq(sequence)
            }

            is MultiAssign -> {

                val first = n.vals.first()
                val stmts: MutableList<IRStmt> = mutableListOf()
                val targetList: List<IRExpr> = n.targets.map {
                    val transl = translateAssignTarget(it, sourceFn)

                    when (it) {
                        is AssignTarget.IdAssign -> {
                            globals.forEach { global ->
                                if (it.idAssign.name == global.name) {// if the LHS is a global
                                    globalsByFunction[sourceFn]?.add(it.idAssign.name)
                                }
                            }
                        }

                        else -> {}
                    }

                    if (transl is IRESeq) { // this fixes order of eval
                        stmts.add(transl.statement)
                        transl.value
                    } else
                        transl
                }

                if (n.vals.size == 1 && first is Expr.FunctionCall) {
                    // go straight to IRCallStmt and do not pass go
                    // get the right number of returns from _RV whatever

                    // DO THE CALL IN HERE DO NOT PASS IT DOWN
                    // assuming that the number of returns must match the number of targets, checked in typecheck

                    functionCalls[sourceFn]?.add(mangledFunctionNames[first.fn]!!)

                    stmts.add(
                        IRCallStmt(IRName(
                            mangledFunctionNames[first.fn]!!
                        ),
                            n.targets.size.toLong(),
                            first.args.map { translateExpr(it, sourceFn) }
                        ))
                    val returnTemps: MutableList<IRTemp> = mutableListOf()
                    for (i in 1..n.targets.size) {
                        returnTemps.add(IRTemp("_RV$i"))
                    }
                    stmts.addAll((targetList zip returnTemps).map { multiAssignMove(it) })
                    IRSeq(stmts)
                } else {
                    val translatedExprs: List<IRExpr> = n.vals.map { translateExpr(it, sourceFn) } // rhs first
                    val exprTempList: MutableList<IRExpr> = mutableListOf()
                    for (it in translatedExprs) {
                        val ti = freshTemp()
                        exprTempList.add(ti)
                        stmts.add(IRMove(ti, it))
                    }
                    val assignList: List<IRStmt> = (targetList zip exprTempList).map { multiAssignMove(it) }
                    stmts.addAll(assignList)
                    IRSeq(stmts)
                }

            }

            is Statement.Procedure -> {
                functionCalls[sourceFn]?.add(mangledFunctionNames[n.id]!!)
                IRCallStmt(IRName(mangledFunctionNames[n.id]!!), 0, n.args.map { translateExpr(it, sourceFn) })
            }

            is Statement.Return -> {
                IRReturn(n.args.map { translateExpr(it, sourceFn) })
            }

            is VarDecl.InitArr -> {
                val moves = mutableListOf<IRStmt>()
                val dimensions = n.arrInit.dimensions.toList().filterNotNull()
                // enforce l to r eval order with reversed
                val evalDims = dimensions.reversed().map {
                    when (val translateDim = translateExpr(it, sourceFn)) {
                        is IRESeq -> {
                            moves.add(translateDim.statement)
                            translateDim.value
                        }

                        else -> {
                            val evalTemp = freshTemp()
                            moves.add(IRMove(evalTemp, translateDim))
                            evalTemp
                        }
                    }
                }
                val (arrLoc, arrInstrs) = arrayInitHelp(evalDims)
                moves.addAll(
                    listOf(
                        arrInstrs,
                        IRMove(IRTemp(n.id), arrLoc)
                    )
                )
                IRSeq(
                    moves
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
                        translateControl(n.guard, trueLabel, falseLabel, sourceFn),
                        trueLabel,
                        translateStatement(n.body, sourceFn),
                        IRJump(IRName(startLabel.l)),
                        falseLabel
                    )
                )
            }
        }
    }

    private fun multiAssignMove(pair: Pair<IRExpr, IRExpr>): IRStmt {
        val fst = pair.first
        val snd = pair.second
        when (fst) {
            is IRTemp -> {
                globals.forEach {
                    if (fst.name == it.name) {// if the LHS is a global
                        return IRMove(IRMem(IRName(fst.name)), snd)
                    }
                }
                // if it was not found in any of the globals, must be local
                return IRMove(fst, snd)
            }

            is IRESeq -> {
                return IRSeq(mutableListOf(fst.statement, IRMove(fst.value, snd)))
            }

            else -> {
                return IRMove(fst, snd)
            }
        }
    }

    // precondition -- lst always has at least one element
    private fun arrayInitHelp(lst: List<IRExpr>): Pair<IRTemp, IRSeq> {
        return if (lst.size <= 1) {
            val tempN = freshTemp()
            val tempM = freshTemp()
            Pair(
                tempM,
                IRSeq(
                    listOf(
                        IRMove(tempN, lst.first()),
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
                IRMove(arrSize, lst.first()),
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
                    IRCJump(IROp(GEQ, counter, arrSize), complete, loopLabel),
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


    private fun translateExpr(n: Expr, sourceFn: String): IRExpr {
        return when (n) {
            is Expr.ArrayAccess -> {
                val tempA = freshTemp()
                val tempI = freshTemp()
                val successLabel = freshLabel()
                val errorLabel = freshLabel()
                IRESeq(
                    IRSeq(
                        listOf(
                            IRMove(tempA, translateExpr(n.arr, sourceFn)),
                            IRMove(tempI, translateExpr(n.idx, sourceFn)),
                            IRCJump(
                                IROp(ULT, tempI, IRMem(IROp(SUB, tempA, IRConst(8)))),
                                successLabel,
                                errorLabel
                            ),
                            errorLabel,
                            IRCallStmt(IRName("_eta_out_of_bounds"), 0, listOf()),
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
                if (opType == ADD && n.etaType is EtaType.OrdinaryType.ArrayType) {
                    // find left and right arrays
                    var translateLeft = translateExpr(n.left, sourceFn)
                    var translateRight = translateExpr(n.right, sourceFn)
                    val moves: MutableList<IRStmt> = mutableListOf()
                    // avoid nesting of duplicate code by raising ESeq stmts, eval fn calls early
                    translateLeft = when (translateLeft) {
                        is IRESeq -> {
                            moves.add(translateLeft.statement)
                            translateLeft.value
                        }

                        else -> {
                            val evalTemp = freshTemp()
                            moves.add(IRMove(evalTemp, translateLeft))
                            evalTemp
                        }
                    }
                    translateRight = when (translateRight) {
                        is IRESeq -> {
                            moves.add(translateRight.statement)
                            translateRight.value
                        }

                        else -> {
                            val evalTemp = freshTemp()
                            moves.add(IRMove(evalTemp, translateRight))
                            evalTemp
                        }
                    }
                    // compute left/right array lengths from memory
                    val tempLeftLength = freshTemp()
                    val tempRightLength = freshTemp()

                    moves.add(IRMove(tempLeftLength, IRMem(IROp(SUB, translateLeft, IRConst(8)))))
                    moves.add(IRMove(tempRightLength, IRMem(IROp(SUB, translateRight, IRConst(8)))))
                    // instantiate new array
                    val newArrLength = freshTemp()
                    val newArrPtrTemp = freshTemp()
                    moves.add(IRMove(newArrLength, IROp(ADD, tempLeftLength, tempRightLength)))
                    moves.addAll(arrayInitMoves(newArrLength, newArrPtrTemp))
                    moves.add(IRMove(newArrPtrTemp, IROp(ADD, newArrPtrTemp, IRConst(8))))

                    // fill the array
                    val loopLabel = freshLabel()
                    val loopBody = freshLabel()
                    val complete = freshLabel()
                    val leftFill = freshLabel()
                    val rightFill = freshLabel()
                    val increment = freshLabel()
                    val counter = freshTemp()
                    val loop = mutableListOf(
                        IRMove(counter, IRConst(0)),
                        loopLabel,
                        IRCJump(IROp(GEQ, counter, newArrLength), complete, loopBody),
                        loopBody,
                        IRCJump(IROp(GEQ, counter, tempLeftLength), rightFill, leftFill),
                        leftFill,
                        IRMove(
                            IRMem(IROp(ADD, newArrPtrTemp, IROp(MUL, counter, IRConst(8)))),
                            IRMem(IROp(ADD, translateLeft, IROp(MUL, counter, IRConst(8))))
                        ),
                        IRJump(IRName(increment.l)),
                        rightFill,
                        IRMove(
                            IRMem(IROp(ADD, newArrPtrTemp, IROp(MUL, counter, IRConst(8)))),
                            IRMem(IROp(ADD, translateRight, IROp(MUL, IROp(SUB, counter, tempLeftLength), IRConst(8))))
                        ),
                        increment,
                        IRMove(counter, IROp(ADD, counter, IRConst(1))),
                        IRJump(IRName(loopLabel.l)),
                        complete
                    )
                    moves.addAll(loop)
                    // finish
                    IRESeq(IRSeq(moves), newArrPtrTemp)
                } else if (opType == AND) {
                    val tempX = freshTemp()
                    val label1 = freshLabel()
                    val label2 = freshLabel()
                    val labelF = freshLabel()
                    IRESeq(
                        IRSeq(
                            listOf(
                                IRMove(tempX, IRConst(0)),
                                IRCJump(translateExpr(n.left, sourceFn), label1, labelF),
                                label1,
                                IRCJump(translateExpr(n.right, sourceFn), label2, labelF),
                                label2,
                                IRMove(tempX, IRConst(1)),
                                labelF
                            )
                        ),
                        tempX
                    )
                } else if (opType == OR) {
                    val tempX = freshTemp()
                    val label1 = freshLabel()
                    val label2 = freshLabel()
                    val labelT = freshLabel()
                    IRESeq(
                        IRSeq(
                            listOf(
                                IRMove(tempX, IRConst(1)),
                                IRCJump(translateExpr(n.left, sourceFn), labelT, label1),
                                label1,
                                IRCJump(translateExpr(n.right, sourceFn), labelT, label2),
                                label2,
                                IRMove(tempX, IRConst(0)),
                                labelT
                            )
                        ), tempX
                    )
                } else {
                    IROp(opType, translateExpr(n.left, sourceFn), translateExpr(n.right, sourceFn))
                }
            }

            is Expr.FunctionCall -> {
                functionCalls[sourceFn]?.add(mangledFunctionNames[n.fn]!!)
                IRCall(IRName(mangledFunctionNames[n.fn]!!), n.args.map { translateExpr(it, sourceFn) })
            }

            is Expr.Identifier -> {
                var foundGlobal = false
                globals.forEach {
                    if (n.name == it.name) {// if the name is a global
                        foundGlobal = true
                    }
                }
                if (foundGlobal) IRMem(IRName(n.name)) else IRTemp(n.name)
            }

            is Expr.FunctionCall.LengthFn -> {
                val lengthTemp = freshTemp()
                val arrTemp = freshTemp()
                IRESeq(
                    IRSeq(
                        listOf(
                            IRMove(arrTemp, translateExpr(n.arg, sourceFn)),
                            IRMove(lengthTemp, IRMem(IROp(SUB, arrTemp, IRConst(8))))
                        )
                    ),
                    lengthTemp
                )
            }

            is Literal.ArrayLit -> {
                val tempM = freshTemp()
                val moves = arrayInitMoves(IRConst(n.list.size.toLong()), tempM)
                for (i in 0 until n.list.size) {
                    moves.add(
                        IRMove(
                            IRMem(IROp(ADD, tempM, IRConst((8 * (i + 1)).toLong()))),
                            translateExpr(n.list[i], sourceFn)
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
                val escapedString = escapeStringChars(n.text)
                val moves = arrayInitMoves(IRConst(escapedString.length.toLong()), stringPtr)

                for (i in escapedString.indices) {
                    moves.add(
                        IRMove(
                            IRMem(IROp(ADD, stringPtr, IRConst((8 * (i + 1)).toLong()))),
                            IRConst(escapedString[i].code.toLong())
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
                UnaryOp.Operation.NOT -> IROp(XOR, IRConst(1), translateExpr(n.arg, sourceFn))
                UnaryOp.Operation.NEG -> IROp(SUB, IRConst(0), translateExpr(n.arg, sourceFn))
            }
        }
    }

    private fun escapeStringChars(s: String): String {
        return s.replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    private fun arrayInitMoves(lstLength: IRExpr, ptr: IRTemp): MutableList<IRMove> {
        val moves = mutableListOf(
            IRMove(
                ptr, // 8 * the length needed
                IRCall(IRName("_eta_alloc"), listOf(IROp(ADD, IROp(MUL, lstLength, IRConst(8)), IRConst(8))))
            ), //IRConst((lstLength * 8 + 8).toLong())))),
            IRMove(IRMem(ptr), lstLength)
        )
        return moves
    }

    private fun translateControl(n: Expr, trueLabel: IRLabel, falseLabel: IRLabel, sourceFn: String): IRStmt {
        return when (n) {
            is Literal.BoolLit -> if (n.bool) IRJump(IRName(trueLabel.l)) else IRJump(IRName(falseLabel.l))
            is UnaryOp -> {
                if (n.op == UnaryOp.Operation.NOT) {
                    translateControl(n.arg, falseLabel, trueLabel, sourceFn)
                } else { //shouldn't typecheck
                    throw Exception("used a non-not unary as a guard, unreachable")
                }
            }

            is BinaryOp -> {
                if (n.op == BinaryOp.Operation.AND) {
                    val label1 = freshLabel()
                    IRSeq(
                        listOf(
                            translateControl(n.left, label1, falseLabel, sourceFn),
                            label1,
                            translateControl(n.right, trueLabel, falseLabel, sourceFn)
                        )
                    )
                } else if (n.op == BinaryOp.Operation.OR) {
                    val label1 = freshLabel()
                    IRSeq(
                        listOf(
                            translateControl(n.left, trueLabel, label1, sourceFn),
                            label1,
                            translateControl(n.right, trueLabel, falseLabel, sourceFn)
                        )
                    )
                } else {
                    IRCJump(translateExpr(n, sourceFn), trueLabel, falseLabel)
                }
            }

            else -> IRCJump(translateExpr(n, sourceFn), trueLabel, falseLabel)
        }
    }
}