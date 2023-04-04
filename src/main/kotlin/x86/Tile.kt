package x86

import com.sun.org.apache.xpath.internal.operations.Bool
import ir.lowered.LIRExpr
import ir.lowered.LIRNode
import ir.lowered.LIRStmt
import ir.lowered.LIRStmt.FlatStmt

sealed class Tile(val cost : Int, val pattern : (LIRNode) -> Pair<Boolean, List<LIRNode>>) {

//    abstract fun patternMatch
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
    * if (no match) {
    *   call the node's own tiling that tiles itself
    * }
    *
    * RootTile(17, (lambda x -> ))
    * */


   /*
         A
       B   C
     X

        A     [rax + rbx * 3 + rbx]  [rax + rbx * 4 MINUS CONST(5)]
      B   C
    X Y

        A
      B   C
    X Z
    */
    sealed class RootTile(cost : Int, pattern : (FlatStmt) -> Pair<Boolean, List<LIRNode>>) : Tile(cost,
        {
            when(it) {
                is FlatStmt -> pattern(it)
                else -> false to listOf()
            }
        }
    ) {
        class MoveTile(cost : Int, pattern : (LIRStmt.LIRMove) -> Pair<Boolean, List<LIRNode>>) : Tile(cost,
            {
                when(it) {
                    is LIRStmt.LIRMove -> pattern(it)
                    else -> false to listOf()
                }
            }
        )
    }

    // we pretend that this is always a FlatStmt

    sealed class ExprTile(cost : Int, pattern : (LIRNode) -> Boolean) : Tile(cost, pattern) {

        sealed class OpTile(cost : Int, pattern : (LIRExpr.LIROp) -> Boolean) {


        }

    }
    // we pretend that this is always an LIRExpr

    fun matchStmt(n1 : FlatStmt, n2 : FlatStmt) : Boolean {
        when (n1) {
            is LIRStmt.LIRCJump -> throw Exception("unreachable, blocks not reordered")
            is LIRStmt.LIRJump -> {
                return if (n2 !is LIRStmt.LIRJump) false
                else {
                    matchExpr(n1.address, n2.address)
                }
            }
            is LIRStmt.LIRReturn -> {
                if (n2 !is LIRStmt.LIRReturn) return false
                else {
                    if (n1.valList.size != n2.valList.size) {
                        return false
                    }
                    else {
                        for (idx in 0 until n1.valList.size) {
                            if (!matchExpr(n1.valList[idx], n2.valList[idx])) return false
                        }
                        return true
                    }
                }
            }
            is LIRStmt.LIRTrueJump -> TODO()
            is LIRStmt.LIRCallStmt -> {
                return true // this is fake
            }
            is LIRStmt.LIRLabel -> TODO()
            is LIRStmt.LIRMove -> TODO()
        }
    }

    fun matchExpr(n1 : LIRExpr, n2 : LIRExpr) : Boolean {

    }

}