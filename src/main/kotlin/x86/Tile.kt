package x86

import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
import ir.lowered.LIRExpr
import ir.lowered.LIRNode
import ir.lowered.LIRStmt
import ir.lowered.LIRStmt.FlatStmt

sealed class Tile(val cost : Int, val pattern : (LIRNode) -> Pair<Boolean, List<LIRExpr>>) {


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

    sealed class RootTile(cost : Int, pattern : (FlatStmt) -> Pair<Boolean, List<LIRExpr>>) : Tile(cost,
        {
            when(it) {
                is FlatStmt -> pattern(it)
                else -> false to listOf()
            }
        }
    ) {
        abstract fun instructions(children: List<Register>) : List<Instruction>

        class MoveTile(cost : Int, pattern : (LIRStmt.LIRMove) -> Pair<Boolean, List<LIRExpr>>) : RootTile(cost,
            {
                when(it) {
                    is LIRStmt.LIRMove -> pattern(it)
                    else -> false to listOf()
                }
            }
        ) {
            override fun instructions(children: List<Register>) : List<Instruction> {
                return emptyList()
            }
        }


    }


    sealed class ExprTile(cost : Int, pattern : (LIRExpr) -> Pair<Boolean, List<LIRExpr>>) : Tile(cost,
        {
            when(it) {
                is LIRExpr -> pattern(it)
                else -> false to listOf()
            }
        }
        ) {
        abstract fun instructions (parent : Register, children : List<Register>) : List<Instruction>


        class OpTile(val op : OpType, cost : Int, pattern : (LIRExpr.LIROp) -> Pair<Boolean, List<LIRExpr>>) : ExprTile(cost,
        {
            when(it) {
                is LIRExpr.LIROp -> pattern(it)
                else -> false to listOf()
            }
        }
        ) {
            override fun instructions(parent : Register, children: List<Register>) : List<Instruction> {
                return when(op) {
                    ADD -> TODO()
                    SUB -> TODO()
                    MUL -> TODO()
                    HMUL -> TODO()
                    DIV -> TODO()
                    MOD -> TODO()
                    AND -> TODO()
                    OR -> TODO()
                    XOR -> TODO()
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
//                listOf<Instruction>(
//
//                )
            }

        }

        class MemTile(cost : Int, pattern : (LIRExpr.LIRMem) -> Pair<Boolean, List<LIRExpr>>) : ExprTile(cost,
            {
                when(it) {
                    is LIRExpr.LIRMem -> pattern(it)
                    else -> false to listOf()
                }
            }
        ) {
            override fun instructions(parent: Register, children: List<Register>): List<Instruction> {
                return listOf(
                    Instruction.MOV(Destination.RegisterDest(parent), Source.MemorySrc(Memory(children[0], null)))
                )
            }
        }

    }


//    fun matchStmt(n1 : FlatStmt, n2 : FlatStmt) : Boolean {
//        when (n1) {
//            is LIRStmt.LIRCJump -> throw Exception("unreachable, blocks not reordered")
//            is LIRStmt.LIRJump -> {
//                return if (n2 !is LIRStmt.LIRJump) false
//                else {
//                    matchExpr(n1.address, n2.address)
//                }
//            }
//            is LIRStmt.LIRReturn -> {
//                if (n2 !is LIRStmt.LIRReturn) return false
//                else {
//                    if (n1.valList.size != n2.valList.size) {
//                        return false
//                    }
//                    else {
//                        for (idx in 0 until n1.valList.size) {
//                            if (!matchExpr(n1.valList[idx], n2.valList[idx])) return false
//                        }
//                        return true
//                    }
//                }
//            }
//            is LIRStmt.LIRTrueJump -> TODO()
//            is LIRStmt.LIRCallStmt -> {
//                return true // this is fake
//            }
//            is LIRStmt.LIRLabel -> TODO()
//            is LIRStmt.LIRMove -> TODO()
//        }
//    }

//    fun matchExpr(n1 : LIRExpr, n2 : LIRExpr) : Boolean {
//
//    }

}