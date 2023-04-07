package assembly

import assembly.x86.Instruction
import assembly.x86.Register
import ir.lowered.LIRExpr
import ir.lowered.LIRNode
import ir.lowered.LIRStmt.*
import ir.lowered.LIRStmt.FlatStmt

sealed class Tile(val cost : Int, val pattern : (LIRNode) -> Pair<Boolean, List<LIRExpr>>) {
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

    sealed class RootTile(cost : Int, pattern : (FlatStmt) -> Pair<Boolean, List<LIRExpr>>,
        val instructions : (FlatStmt, List<Register>) -> List<Instruction>) : Tile(cost,
        {
            when(it) {
                is FlatStmt -> pattern(it)
                else -> false to listOf()
            }
        }
    ) {

        class MoveTile(cost : Int, pattern : (LIRMove) -> Pair<Boolean, List<LIRExpr>>,
            instructions: (LIRMove, List<Register>) -> List<Instruction>) : RootTile(cost,
            {
                when(it) {
                    is LIRMove -> pattern(it)
                    else -> false to listOf()
                }
            },
            { lirmove, reglst ->
                when (lirmove) {
                    is LIRMove -> instructions(lirmove, reglst)
                    else -> listOf()
                }
            })

        class JumpTile(cost : Int, pattern : (LIRJump) -> Pair<Boolean, List<LIRExpr>>,
                       instructions: (LIRJump, List<Register>) -> List<Instruction>) : RootTile(cost,
            {
                when(it) {
                    is LIRJump -> pattern(it)
                    else -> false to listOf()
                }
            }, { lirjump, reglst ->
                when (lirjump) {
                    is LIRJump -> instructions(lirjump, reglst)
                    else -> listOf()
                }
            })

        class CJumpTile(cost : Int, pattern : (LIRTrueJump) -> Pair<Boolean, List<LIRExpr>>,
                       instructions: (LIRTrueJump, List<Register>) -> List<Instruction>) : RootTile(cost,
            {
                when(it) {
                    is LIRTrueJump -> pattern(it)
                    else -> false to listOf()
                }
            }, { lircjump, reglst ->
                when (lircjump) {
                    is LIRTrueJump -> instructions(lircjump, reglst)
                    else -> listOf()
                }
            })

        class ReturnTile(cost : Int, pattern : (LIRReturn) -> Pair<Boolean, List<LIRExpr>>,
                        instructions: (LIRReturn, List<Register>) -> List<Instruction>) : RootTile(cost,
            {
                when(it) {
                    is LIRReturn -> pattern(it)
                    else -> false to listOf()
                }
            }, { lirret, reglst ->
                when (lirret) {
                    is LIRReturn -> instructions(lirret, reglst)
                    else -> listOf()
                }
            })
    }


    sealed class ExprTile(cost : Int, pattern : (LIRExpr) -> Pair<Boolean, List<LIRExpr>>,
        val instructions : (Register, List<Register>) -> List<Instruction>) : Tile(cost,
        {
            when(it) {
                is LIRExpr -> pattern(it)
                else -> false to listOf()
            }
        }
        ) {

        class OpTile(cost : Int, pattern : (LIRExpr.LIROp) -> Pair<Boolean, List<LIRExpr>>,
            instructions : (Register, List<Register>) -> List<Instruction>) : ExprTile(cost,
        {
            when(it) {
                is LIRExpr.LIROp -> pattern(it)
                else -> false to listOf()
            }
        },
            instructions)
//        {
//            override fun instructions(parent : Register, children: List<Register>) : List<Instruction> {
//                return when(op) {
//                    ADD -> TODO()
//                    SUB -> TODO()
//                    MUL -> TODO()
//                    HMUL -> TODO()
//                    DIV -> TODO()
//                    MOD -> TODO()
//                    AND -> TODO()
//                    OR -> TODO()
//                    XOR -> TODO()
//                    LSHIFT -> TODO()
//                    RSHIFT -> TODO()
//                    ARSHIFT -> TODO()
//                    EQ -> TODO()
//                    NEQ -> TODO()
//                    LT -> TODO()
//                    ULT -> TODO()
//                    GT -> TODO()
//                    LEQ -> TODO()
//                    GEQ -> TODO()
//                }
//            }

        }

        class MemTile(cost : Int, pattern : (LIRExpr.LIRMem) -> Pair<Boolean, List<LIRExpr>>,
            instructions: (Register, List<Register>) -> List<Instruction>) : ExprTile(cost,
            {
                when(it) {
                    is LIRExpr.LIRMem -> pattern(it)
                    else -> false to listOf()
                }
            }, instructions) {
//            override fun instructions(parent: Register, children: List<Register>): List<Instruction> {
//                return listOf(
//                    Instruction.MOV(Destination.RegisterDest(parent), Source.MemorySrc(Memory(children[0], null)))
//                )
//            }
//        }

    }

}