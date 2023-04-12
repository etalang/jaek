package assembly

import ir.IRData
import ir.lowered.*
import ir.lowered.LIRStmt.*
import ir.lowered.LIRExpr.*
import assembly.Tile.*
import assembly.Tile.RootTile.*
import assembly.Tile.ExprTile.*
import assembly.x86.Instruction.*
import assembly.x86.Instruction.Jump.*
import assembly.x86.Destination.*
import assembly.x86.Register.*
import assembly.x86.Source.*
import assembly.x86.Memory.*
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*

class Tiler(val IR: LIRCompUnit) {
    private var freshRegisterCount = 0
    private fun freshRegister(): Register {
        freshRegisterCount++
        return Register.Abstract("\$A$freshRegisterCount")
    }

    // TILE DEFINITIONS
    private val jumpTiles = listOf(
        JumpTile(1) {
            TileAttempt(true,
                instrs = listOf(JMP(Location(Label(it.address.l, false)))))
        }
    )
    private val returnTiles = listOf(
        ReturnTile(1) {
            val insns = mutableListOf<Instruction>()
            val reglst = mutableListOf<Register>()
            if (it.valList.isNotEmpty()) { // single return
                    reglst.add(freshRegister())
                    insns.add(MOV(RegisterDest(x86(x86Name.RAX)), RegisterSrc(reglst[0])))
            }
            if (it.valList.size > 1) { // multireturn
                reglst.add(freshRegister())
                insns.add(MOV(RegisterDest(x86(x86Name.RDX)), RegisterSrc(reglst[1])))
            }
            if (it.valList.size > 2) { // begin da push
                for (i in it.valList.size - 1 downTo 3 ) {
                    reglst.add(freshRegister())
                    insns.add(MOV(
                        MemoryDest(
                            RegisterMem(x86(x86Name.RDI), null,
                            offset= 8L * (i - 3L))
                        ),
                        RegisterSrc(reglst[i])))
                }
            }
            // TODO: test whether this works/ensure that the invariants are preserved so that this works
            insns.add(LEAVE())
            insns.add(RET())
//            assert(it.valList.size == reglst.size) aaserted in init
            TileAttempt(true, it.valList, reglst, insns)
        }
    )
    private val cjumpTiles = listOf(
        CJumpTile(2) {
            val guardReg = freshRegister()
            TileAttempt(true, listOf(it.guard), listOf(guardReg),
                listOf(
                    TEST(guardReg, guardReg),
                    JNZ(Location(Label(it.trueBranch.l, false)))
                )
            )
        }
    )
    private val moveTiles = listOf(
        MoveTile(1,
        ) { lirMove: LIRMove ->
            if (lirMove.dest is LIRMem) {
                val srcReg = freshRegister()
                if (lirMove.dest.address is LIRName) {
                    TileAttempt(
                        true, listOf(lirMove.expr), listOf(srcReg),
                        listOf(
                            MOV(
                                MemoryDest(LabelMem(Label(lirMove.dest.address.l, false))),
                                RegisterSrc(srcReg)
                            )
                        )
                    )
                } else {
                    val destReg = freshRegister()
                    TileAttempt(
                        true, listOf(lirMove.dest.address, lirMove.expr), listOf(destReg, srcReg),
                        listOf(MOV(MemoryDest(RegisterMem(destReg, null)), RegisterSrc(srcReg)))
                    )
                }
            } else {
                TileAttempt(false)
            }
        },
        MoveTile(1,
        ) { lirMove: LIRMove ->
            if (lirMove.dest is LIRTemp) {
                val srcReg = freshRegister()
                TileAttempt(
                    true, listOf(lirMove.expr), listOf(srcReg),
                    listOf(MOV(RegisterDest(Abstract((lirMove.dest).name)), RegisterSrc(srcReg)))
                )
            } else {
                TileAttempt(false)
            }
        }
    )
    private val callTiles = listOf<RootTile>(
        CallTile(1) {
            val insns = mutableListOf<Instruction>()
            val reglst = mutableListOf<Register>()
            val argNumber = it.args.size
            for (i in 0 until argNumber)
                reglst.add(freshRegister())

            if (it.n_returns >= 3) {
                // can only store 5 arguments in registers
                // TODO: make sure this aligns with how we store return values
                insns.add(Arith.SUB(RegisterDest(x86(x86Name.RSP)), ConstSrc(8L * (it.n_returns - 2L))))
                insns.add(MOV(RegisterDest(x86(x86Name.RDI)), RegisterSrc(x86(x86Name.RSP))))
                if (argNumber > 5) {
                    for (i in it.args.size - 1 downTo 5) {
                        insns.add(PUSH(reglst[i]))
                    }
                }
                if (argNumber > 4) {
                    insns.add(MOV(RegisterDest(x86(x86Name.R9)), RegisterSrc(reglst[4])))
                }
                if (argNumber > 3) {
                    insns.add(MOV(RegisterDest(x86(x86Name.R8)), RegisterSrc(reglst[3])))
                }
                if (argNumber > 2) {
                    insns.add(MOV(RegisterDest(x86(x86Name.RCX)), RegisterSrc(reglst[2])))
                }
                if (argNumber > 1) {
                    insns.add(MOV(RegisterDest(x86(x86Name.RDX)), RegisterSrc(reglst[1])))
                }
                if (it.args.isNotEmpty()) {
                    insns.add(MOV(RegisterDest(x86(x86Name.RSI)), RegisterSrc(reglst[1])))
                }
//                    insns.add(Logic.AND(RegisterDest(x86(x86Name.RSP)), ConstSrc(-16)))
                insns.add(CALL(Label(it.target.l, false)))
                insns.add(Logic.AND(RegisterDest(x86(x86Name.RSP)), ConstSrc(-16)))
                if (argNumber > 5) {
                    insns.add(Arith.ADD(RegisterDest(x86(x86Name.RSP)), ConstSrc(8L * (argNumber - 5L))))
                }
                insns.add(MOV(RegisterDest(Abstract("_RV1")), RegisterSrc(x86(x86Name.RAX))))
                insns.add(MOV(RegisterDest(Abstract("_RV2")), RegisterSrc(x86(x86Name.RDX))))
                for (i in 3 .. it.n_returns.toInt()) {
                    insns.add(POP(Abstract("_RV$i")))
                }
            }
            else {
                // can only store 5 arguments in registers
                if (argNumber > 6) {
                    for (i in it.args.size - 1 downTo 6) {
                        insns.add(PUSH(reglst[i]))
                    }
                }
                if (argNumber > 5) {
                    insns.add(MOV(RegisterDest(x86(x86Name.R9)), RegisterSrc(reglst[5])))
                }
                if (argNumber > 4) {
                    insns.add(MOV(RegisterDest(x86(x86Name.R8)), RegisterSrc(reglst[4])))
                }
                if (argNumber > 3) {
                    insns.add(MOV(RegisterDest(x86(x86Name.RCX)), RegisterSrc(reglst[3])))
                }
                if (argNumber > 2) {
                    insns.add(MOV(RegisterDest(x86(x86Name.RDX)), RegisterSrc(reglst[2])))
                }
                if (argNumber > 1) {
                    insns.add(MOV(RegisterDest(x86(x86Name.RSI)), RegisterSrc(reglst[1])))
                }
                if (it.args.isNotEmpty()) {
                    insns.add(MOV(RegisterDest(x86(x86Name.RDI)), RegisterSrc(reglst[0])))
                }
                insns.add(CALL(Label(it.target.l, false)))
                insns.add(MOV(RegisterDest(Abstract("_RV1")), RegisterSrc(x86(x86Name.RAX))))
                insns.add(MOV(RegisterDest(Abstract("_RV2")), RegisterSrc(x86(x86Name.RDX))))
                if (argNumber > 6) {
                    insns.add(Arith.ADD(RegisterDest(x86(x86Name.RSP)), ConstSrc(8L * (argNumber.toLong() - 6))))
                }
            }

            TileAttempt(true, it.args, reglst, insns)
        }
    )

    /** refactoring LIRExpr.Op instruction production */
    private val opMunch : (IRBinOp.OpType) -> ((LIROp, Register) -> TileAttempt) =
        {
            opType : IRBinOp.OpType ->
            { n: LIROp, parent : Register ->
                if (n.op == opType) {
                    val leftReg = freshRegister()
                    val rightReg = freshRegister()
                    val regList = listOf(leftReg, rightReg)
                    TileAttempt(true, listOf(n.left, n.right), regList,
                        opInstructions(opType)(parent, regList))
                } else TileAttempt(false)
            }
        }
    private val opInstructions : (IRBinOp.OpType) -> ((Register, List<Register>) -> List<Instruction>) =
        {
            opType ->
            {
                parent, children ->
                    when(opType) {
                        IRBinOp.OpType.ADD ->
                            listOf(MOV(RegisterDest(parent), RegisterSrc(children[0])),
                                Arith.ADD(RegisterDest(parent), RegisterSrc(children[1])))
                        IRBinOp.OpType.SUB ->
                            listOf(MOV(RegisterDest(parent), RegisterSrc(children[0])),
                                Arith.SUB(RegisterDest(parent), RegisterSrc(children[1])))
                        IRBinOp.OpType.MUL -> listOf(MOV(RegisterDest(parent), RegisterSrc(children[0])),
                            Arith.MUL(RegisterDest(parent), RegisterSrc(children[1])))
                        HMUL -> TODO()
                        IRBinOp.OpType.DIV -> TODO()
                        MOD -> TODO()
                        AND -> listOf(MOV(RegisterDest(parent), RegisterSrc(children[0])),
                            Logic.AND(RegisterDest(parent), RegisterSrc(children[1])))
                        OR -> listOf(MOV(RegisterDest(parent), RegisterSrc(children[0])),
                            Logic.OR(RegisterDest(parent), RegisterSrc(children[1])))
                        XOR -> listOf(MOV(RegisterDest(parent), RegisterSrc(children[0])),
                            Logic.XOR(RegisterDest(parent), RegisterSrc(children[1])))
                        LSHIFT -> TODO()
                        RSHIFT -> TODO()
                        ARSHIFT -> TODO()
                        EQ -> TODO()
                        NEQ -> TODO()
                        LT -> TODO()
                        ULT -> TODO()
                        GT -> TODO()
                        LEQ -> TODO()
                        GEQ -> TODO()
                    }
            }
        }

    private val exprTiles = listOf<ExprTile>(
        MemTile(1) { lirMem, parent ->
            if (lirMem.address !is LIRName) {
                val addrReg = freshRegister()
                TileAttempt(true, listOf(lirMem.address), listOf(addrReg),
                    listOf(MOV(RegisterDest(parent), MemorySrc(RegisterMem(addrReg, null)))))
            } else TileAttempt(false)
        },
        MemTile(1) {
            lirMem, parent ->
            if (lirMem.address is LIRName) {
                TileAttempt(true, instrs = listOf(
                    MOV(RegisterDest(parent),
                    MemorySrc(LabelMem(Label(lirMem.address.l, false)))))
                )
            } else TileAttempt(false)
        },
        OpTile(2, opMunch(IRBinOp.OpType.ADD)),
        OpTile(2, opMunch(IRBinOp.OpType.SUB)),
        OpTile(2, opMunch(IRBinOp.OpType.MUL)),
        OpTile(2, opMunch(IRBinOp.OpType.AND)),
        OpTile(2, opMunch(IRBinOp.OpType.OR)),
        OpTile(2, opMunch(IRBinOp.OpType.XOR)),
    )




    fun tile() : x86CompUnit {
        return tileCompUnit(IR)
    }

    private fun tileCompUnit(n : LIRCompUnit) : x86CompUnit {
        val assemblyFuncs : MutableList<x86FuncDecl> = ArrayList()
        n.functions.forEach { assemblyFuncs.add(tileFuncDecl(it)) }
        val assemblyData : MutableList<x86Data> = ArrayList()
        n.globals.forEach { assemblyData.add(tileData(it)) }
        return x86CompUnit(n.name, assemblyFuncs, assemblyData)
    }

    private fun tileData(n : IRData) : x86Data {
        return x86Data(n.name, n.data)
    }

    private fun tileFuncDecl(n : LIRFuncDecl) : x86FuncDecl {
        val insnBlock : List<Instruction> = tileSeq(n.body)
        return x86FuncDecl(n.name, insnBlock)
    }

    private fun tileSeq(n : LIRSeq) : List<Instruction> {
        var insns = mutableListOf<Instruction>()
        for (stmt in n.block) {
//            insns.add(NOP()) // debug
//            insns.add(NOP()) // debug
//            insns.add(NOP()) // debug
//            insns.add(NOP()) // debug
            insns.addAll(tileTree(stmt))
        }
        // TODO: do register allocation here
        // TODO: add preamble (currently a full guess)
        val ra = RegisterAllocator()
        insns = ra.allocateRegisters(insns)
        return insns
    }

    private var memoizedExprs : MutableMap<LIRExpr, Pair<Int, List<Instruction>>> = mutableMapOf()

    /*
* // suppose tree = LIRNode we care about
* // suppose tilemap = map of tiles organized by roots
*
* for (t : tilemap[tree]) {
*   (b, t) = t.pattern(tree)
*   if (b) {
*       for (subtree : lst) {
*           // try and tile the subtrees
*           // memoize the results
*       }
*   }
* }
* */

    private fun tileTree(n : FlatStmt) : List<Instruction> {
        // clear the memoization table before each new tiling of a statement
        memoizedExprs = mutableMapOf()
        val rootTiles = when (n) {
            is LIRCJump -> throw Exception("Un-block-reordered IR")
            is LIRLabel -> return listOf(Label(n.l, true))
            is LIRJump -> jumpTiles
            is LIRReturn -> returnTiles
            is LIRTrueJump -> cjumpTiles
            is LIRCallStmt -> callTiles
            is LIRMove -> moveTiles
        }

        var minCost = Int.MAX_VALUE
        var minInsns = listOf<Instruction>()
        for (t in rootTiles) {
            val attempt = t.munch(n)
            if (attempt.match) {
                var currCost = t.cost
                val currInsns = mutableListOf<Instruction>()
                for (i in 0 until attempt.subtrees.size) {
                    val cost : Int
                    val insns : List<Instruction>
                    val subtree = attempt.subtrees[i]
                    val regEdge = attempt.subregs[i]
                    if (subtree in memoizedExprs.keys) {
                        // this should never throw an exception, for I have literally just checked it
                        memoizedExprs[subtree]!!.let{ (c, il) -> cost = c; insns = il}
                    }
                    else {
                        tileExprSubtree(subtree, regEdge).let{ (c, il) -> cost = c; insns = il  }
                        memoizedExprs[subtree] = Pair(cost, insns)
                    }
                    currCost += cost
                    currInsns.addAll(insns)
                }
                // convert the current tile into instructions using the register
                val tileInsns = attempt.instrs
                currInsns.addAll(tileInsns)
                if (currCost < minCost) {
                    minCost = currCost
                    minInsns = currInsns
                }
            }
        }
        return minInsns

    }

    /** tileExprSubtree(n) does the heavy lifting to tile expression subtrees */
    private fun tileExprSubtree(n : LIRExpr, reg : Register) : Pair<Int, List<Instruction>>  {
        if (memoizedExprs.contains(n)) {
            return memoizedExprs[n]!!
        }
        else {
            when (n) {
                is LIRExpr.LIRName -> {
                    return Pair(0, listOf(Label(n.l, false)))
                }
                // explicit base cases where there's nothing to do
                is LIRExpr.LIRConst -> {
                    val movInsn = MOV(RegisterDest(reg), ConstSrc(n.value))
                    val costPair = Pair(1, listOf(movInsn))
                    memoizedExprs[n] = costPair
                    return costPair
                }
                is LIRExpr.LIRTemp -> {
                    val movInsn = MOV(RegisterDest(reg), RegisterSrc(Register.Abstract(n.name)))
                    val costPair = Pair(1, listOf(movInsn))
                    memoizedExprs[n] = costPair
                    return costPair
                }
                else -> {
                    var minCost = Int.MAX_VALUE
                    var minInsns = listOf<Instruction>()
                    for (tile in exprTiles) {
                        val attempt = tile.munch(n, reg)
                        if (attempt.match) {
                            var currCost = tile.cost
                            val currInsns = mutableListOf<Instruction>()
                            for (i in 0 until attempt.subtrees.size) {
                                // TODO: this needs to do DP
                                val (cost, insns) = tileExprSubtree(attempt.subtrees[i], attempt.subregs[i])
                                currCost += cost
                                currInsns.addAll(insns)
                            }
                            // convert the current tile into instructions using the register
                            val tileInsns = attempt.instrs
                            currInsns.addAll(tileInsns)
                            if (currCost < minCost) {
                                minCost = currCost
                                minInsns = currInsns
                            }
                        }
                    }
                    val costData = Pair(minCost, minInsns)
                    memoizedExprs[n] = costData
                    return costData
                }
            }
        }
    }


}