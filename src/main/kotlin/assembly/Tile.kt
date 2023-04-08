package assembly

import assembly.x86.Instruction
import assembly.x86.Register
import ir.lowered.LIRExpr
import ir.lowered.LIRNode
import ir.lowered.LIRStmt.*
import ir.lowered.LIRStmt.FlatStmt

sealed class Tile(val cost : Int, val pattern : (LIRNode) -> Pair<Boolean, List<LIRExpr>>) {

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

        class CallTile(cost : Int, pattern : (LIRCallStmt) -> Pair<Boolean, List<LIRExpr>>,
                       instructions: (LIRCallStmt, List<Register>) -> List<Instruction>) : RootTile(cost,
            {
                when(it) {
                    is LIRCallStmt -> pattern(it)
                    else -> false to listOf()
                }
            }, { lircall, reglst ->
                when (lircall) {
                    is LIRCallStmt -> instructions(lircall, reglst)
                    else -> listOf()
                }
            })
    }


    sealed class ExprTile(cost : Int, pattern : (LIRExpr) -> Pair<Boolean, List<LIRExpr>>,
        val instructions : (LIRExpr, Register, List<Register>) -> List<Instruction>) : Tile(cost,
        {
            when(it) {
                is LIRExpr -> pattern(it)
                else -> false to listOf()
            }
        }
        ) {

        class OpTile(cost : Int, pattern : (LIRExpr.LIROp) -> Pair<Boolean, List<LIRExpr>>,
            instructions : (LIRExpr.LIROp, Register, List<Register>) -> List<Instruction>) : ExprTile(cost,
        {
            when(it) {
                is LIRExpr.LIROp -> pattern(it)
                else -> false to listOf()
            }
        },
            { n, parent, children ->
                when(n) {
                    is LIRExpr.LIROp -> instructions(n, parent, children)
                    else -> listOf()
                }
            })

        class MemTile(cost : Int, pattern : (LIRExpr.LIRMem) -> Pair<Boolean, List<LIRExpr>>,
                    instructions: (LIRExpr.LIRMem, Register, List<Register>) -> List<Instruction>) : ExprTile(cost,
            {
                when(it) {
                    is LIRExpr.LIRMem -> pattern(it)
                    else -> false to listOf()
                }
             }, { n, parent, children ->
                when(n) {
                    is LIRExpr.LIRMem -> instructions(n, parent, children)
                    else -> listOf()
                }
            })
        }



}