package assembly

import ir.IRData
import ir.lowered.*
import ir.lowered.LIRStmt.*
import assembly.Tile.*
import assembly.Tile.RootTile.*
import assembly.x86.Instruction.*
import assembly.x86.Instruction.Jump.*
import assembly.x86.Destination.*
import assembly.x86.Register.*
import assembly.x86.Source.*
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*

class Tiler(val IR: LIRCompUnit) {
    private var freshRegisterCount = 0
    private fun freshRegister(): Register {
        freshRegisterCount++
        return Register.Abstract("\$A$freshRegisterCount")
    }

    // TILE DEFINITIONS
    private val jumpTiles = listOf<JumpTile>(
        JumpTile(1, { Pair(true, listOf()) }, {
            lirjump, _ ->
            listOf(JMP(Location(Label(lirjump.address.l, false))))
        })
    )
    private val returnTiles = listOf<ReturnTile>(
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
                                Memory(x86(x86Name.RDI), null,
                                offset= (8*(lirret.valList.size - 1 - i)).toLong())
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
    private val cjumpTiles = listOf<CJumpTile>(
        CJumpTile(2,
            { Pair(true, listOf(it.guard)) },
            { lircjump, reglst ->
                listOf(
                    TEST(reglst[0], reglst[0]),
                    JNZ(Location(Label(lircjump.trueBranch.l, false)))) })
    )
    private val moveTiles = listOf<MoveTile>(
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

    )
    private val exprTiles = listOf<ExprTile>(
        ExprTile.OpTile(
            2,
            {
                if (it.op == ADD) {
                    Pair(true, listOf(it.left, it.right))
                } else Pair(false, listOf())
            },
            {
                parent, children ->
                listOf(
                    MOV(RegisterDest(parent), RegisterSrc(children[0])),
                    Arith.ADD(RegisterDest(parent), RegisterSrc(children[1]))
                )
            }
        )

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
            insns.addAll(tileTree(stmt))
        }
        return insns
    }

    private var memoizedExprs : MutableMap<LIRExpr, Pair<Int, List<Instruction>>> = mutableMapOf()

    // TODO: IMPLEMENT TILING (HARD)
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
                is LIRExpr.LIRName -> { throw Exception("shouldn't be able to tile a name")
//                    return Pair(0, listOf(Label(n.l, false)))
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
                                val (cost, insns) = tileExprSubtree(subtree, regEdge)
                                currCost += cost
                                currInsns.addAll(insns)
                            }
                            // convert the current tile into instructions using the register
                            val tileInsns = tile.instructions(reg, edges)
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