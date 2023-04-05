package x86

import ir.IRData
import ir.lowered.*
import ir.mid.IRExpr

class Tiler(val IR: LIRCompUnit) {
    private var freshRegisterCount = 0
    private val jumpTiles = listOf<Tile.RootTile>()
    private val returnTiles = listOf<Tile.RootTile>()
    private val cjumpTiles = listOf<Tile.RootTile>()
    private val moveTiles = listOf<Tile.RootTile>()
    private val callTiles = listOf<Tile.RootTile>()
    private val exprTiles = listOf<Tile.ExprTile>(

    )


    private fun freshRegister(): Register {
        freshRegisterCount++
        return Register.Abstract("\$A$freshRegisterCount")
    }

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
    private fun tileTree(n : LIRStmt.FlatStmt) : List<Instruction> {
        // clear the memoization table before each new tiling of a statement
        memoizedExprs = mutableMapOf()
        val rootTiles = when (n) {
            is LIRStmt.LIRCJump -> throw Exception("Un-block-reordered IR")
            is LIRStmt.LIRLabel -> return listOf(Label(n.l))
            is LIRStmt.LIRJump -> jumpTiles
            is LIRStmt.LIRReturn -> returnTiles
            is LIRStmt.LIRTrueJump -> cjumpTiles
            is LIRStmt.LIRCallStmt -> callTiles
            is LIRStmt.LIRMove -> moveTiles
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
                val tileInsns = t.instructions(edges)
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
                is LIRExpr.LIRName -> throw Exception("should never be trying to tile a name on its own")
                // explicit base cases where there's nothing to do
                is LIRExpr.LIRConst -> {
                    val movInsn = Instruction.MOV(Destination.RegisterDest(reg), Source.ConstSrc(n.value))
                    val costPair = Pair(1, listOf(movInsn))
                    memoizedExprs[n] = costPair
                    return costPair
                }
                is LIRExpr.LIRTemp -> {
                    val movInsn = Instruction.MOV(Destination.RegisterDest(reg),
                        Source.RegisterSrc(Register.Abstract(n.name)))
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