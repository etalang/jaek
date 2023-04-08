package assembly

import ir.IRData
import ir.lowered.*
import ir.lowered.LIRStmt.*
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
import assembly.x86.Instruction.Arith.*
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
        JumpTile(1, { Pair(true, listOf()) }, {
            lirjump, _ ->
            listOf(JMP(Location(Label(lirjump.address.l, false))))
        })
    )
    private val returnTiles = listOf(
        ReturnTile(1,
            { Pair(true, it.valList) },
            { lirret, reglst ->
                // PRECONDITION: lirret and reglst have the same length
                val insns = mutableListOf<Instruction>()
                if (lirret.valList.isNotEmpty()) { // single return
                    insns.add(MOV(RegisterDest(x86(x86Name.RAX)), RegisterSrc(reglst[0])))
                }
                if (lirret.valList.size > 1) { // multireturn
                    insns.add(MOV(RegisterDest(x86(x86Name.RDX)), RegisterSrc(reglst[1])))
                }
                if (lirret.valList.size > 2) { // begin da push
                    for (i in lirret.valList.size - 1 downTo 3 )
                        insns.add(MOV(
                            MemoryDest(
                                RegisterMem(x86(x86Name.RDI), null,
                                offset= 8L * (i - 3L))
                            ),
                            RegisterSrc(reglst[i])))
                }
                // TODO: test whether this works/ensure that the invariants are preserved so that this works
                insns.add(LEAVE())
                insns.add(RET())
                insns
            }
        )
    )
    private val cjumpTiles = listOf(
        CJumpTile(2,
            { Pair(true, listOf(it.guard)) },
            { lircjump, reglst ->
                listOf(
                    TEST(reglst[0], reglst[0]),
                    JNZ(Location(Label(lircjump.trueBranch.l, false)))) })
    )
    private val moveTiles = listOf(
        // base tile
        MoveTile(1,
            { lirMove: LIRMove ->
                Pair(true, listOf(lirMove.dest, lirMove.expr))
            },
            { _, it ->
                listOf(MOV(RegisterDest(it[0]), RegisterSrc(it[1])))
            }
        )
    )
    private val callTiles = listOf<RootTile>(
        CallTile(
            1,
            { lirCallStmt ->
                // DO NOT give the address of the function -- we absorb it automatically, since our Tiler will blow up on NAMEs.
                Pair(true, lirCallStmt.args)
            },
            {
                lirCallStmt, reglst ->
                val insns = mutableListOf<Instruction>()
                val argNumber = lirCallStmt.args.size
                if (lirCallStmt.n_returns >= 3) {
                    // can only store 5 arguments in registers
                    // TODO: make sure this aligns with how we store return values
                    insns.add(Arith.SUB(RegisterDest(x86(x86Name.RSP)), ConstSrc(8L * (lirCallStmt.n_returns - 2L))))
                    insns.add(MOV(RegisterDest(x86(x86Name.RDI)), RegisterSrc(x86(x86Name.RSP))))
                    if (argNumber > 5) {
                        for (i in lirCallStmt.args.size - 1 downTo 5) {
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
                    if (lirCallStmt.args.isNotEmpty()) {
                        insns.add(MOV(RegisterDest(x86(x86Name.RSI)), RegisterSrc(reglst[1])))
                    }
                    insns.add(CALL(Label(lirCallStmt.target.l, false)))
                    if (argNumber > 5) {
                        insns.add(Arith.ADD(RegisterDest(x86(x86Name.RSP)), ConstSrc(8L * (argNumber.toLong() - 5))))
                    }
                    insns.add(MOV(RegisterDest(Abstract("_RV1")), RegisterSrc(x86(x86Name.RAX))))
                    insns.add(MOV(RegisterDest(Abstract("_RV2")), RegisterSrc(x86(x86Name.RDX))))
                    for (i in 3 .. lirCallStmt.n_returns.toInt()) {
                        insns.add(POP(Abstract("_RV$i")))
                    }
                }
                else {
                    // can only store 5 arguments in registers
                    if (argNumber > 6) {
                        for (i in lirCallStmt.args.size - 1 downTo 6) {
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
                    if (lirCallStmt.args.isNotEmpty()) {
                        insns.add(MOV(RegisterDest(x86(x86Name.RDI)), RegisterSrc(reglst[0])))
                    }
                    insns.add(CALL(Label(lirCallStmt.target.l, false)))
                    insns.add(MOV(RegisterDest(Abstract("_RV1")), RegisterSrc(x86(x86Name.RAX))))
                    insns.add(MOV(RegisterDest(Abstract("_RV2")), RegisterSrc(x86(x86Name.RDX))))
                    if (argNumber > 6) {
                        insns.add(Arith.ADD(RegisterDest(x86(x86Name.RSP)), ConstSrc(8L * (argNumber.toLong() - 6))))
                    }
                }
                insns
            }
        )
    )

    /** refactoring LIRExpr.Op instruction production */
    private val opPattern : (IRBinOp.OpType) -> ((LIRExpr.LIROp) -> (Pair<Boolean, List<LIRExpr>>)) =
        {
            opType : IRBinOp.OpType ->
            { n: LIRExpr.LIROp ->
                if (n.op == opType) {
                    Pair(true, listOf(n.left, n.right))
                } else Pair(false, listOf())
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
                        IRBinOp.OpType.MUL -> TODO()
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

    private val exprTiles = listOf(
        MemTile(
         1,
            { lirMem ->
                if (lirMem.address is LIRExpr.LIRTemp
                    || lirMem.address is LIRExpr.LIRConst
                    || lirMem.address is LIRExpr.LIROp)
                    true to listOf(lirMem.address)
                else false to listOf()
            },
            { _, parent, children ->
                listOf(MOV(RegisterDest(parent), MemorySrc(RegisterMem(children[0], null)))) }
        ),
        MemTile(
            1,
            { lirMem ->
                if (lirMem.address is LIRExpr.LIRName)
                    true to emptyList()
                else false to emptyList()
            },
            { node, parent, _ ->
                listOf(MOV(RegisterDest(parent),
                    MemorySrc(LabelMem(Label((node.address as LIRExpr.LIRName).l, false)))))
            }
        ),
        OpTile(2, { opPattern(IRBinOp.OpType.ADD)(it) },
            {  _, parent, children -> opInstructions(IRBinOp.OpType.ADD)(parent, children) }
        ),
        OpTile(2, { opPattern(IRBinOp.OpType.SUB)(it) },
            {  _, parent, children -> opInstructions(IRBinOp.OpType.SUB)(parent, children) }
        ),
        OpTile(2, { opPattern(IRBinOp.OpType.AND)(it) },
            {  _, parent, children -> opInstructions(IRBinOp.OpType.AND)(parent, children) }
        ),
        OpTile(2, { opPattern(IRBinOp.OpType.OR)(it) },
            { _, parent, children -> opInstructions(IRBinOp.OpType.OR)(parent, children) }
        ),
        OpTile(2, { opPattern(IRBinOp.OpType.XOR)(it) },
            { _, parent, children -> opInstructions(IRBinOp.OpType.XOR)(parent, children) }
        ),

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
            val (b, trees) = t.pattern(n)
            if (b) {
                var currCost = t.cost
                val currInsns = mutableListOf<Instruction>()
                val edges = mutableListOf<Register>()
                for (subtree in trees) {
                    val cost : Int
                    val insns : List<Instruction>
                    val regEdge = freshRegister()
                    edges.add(regEdge)
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
                val tileInsns = t.instructions(n, edges)
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
                        val (b, trees) = tile.pattern(n)
                        if (b) {
                            var currCost = tile.cost
                            val currInsns = mutableListOf<Instruction>()
                            val edges = mutableListOf<Register>()
                            for (subtree in trees) {
                                val regEdge = freshRegister()
                                edges.add(regEdge)
                                // TODO: this needs to do DP
                                val (cost, insns) = tileExprSubtree(subtree, regEdge)
                                currCost += cost
                                currInsns.addAll(insns)
                            }
                            // convert the current tile into instructions using the register
                            val tileInsns = tile.instructions(n, reg, edges)
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