package assembly

import assembly.x86.Instruction
import assembly.x86.Register
import ir.lowered.*
import ir.lowered.LIRStmt.*
import ir.lowered.LIRStmt.FlatStmt

sealed class Tile(val cost: Int) {

    /** INVARIANT: the length of subtrees and subregs MUST be equal. */
    data class TileAttempt(
        val match: Boolean,
        val subtrees: List<LIRExpr> = listOf(),
        val subregs: List<Register> = listOf(),
        val instrs: List<Instruction> = listOf()
    ) {
        init {
            require(subtrees.size == subregs.size)
        }
    }

    sealed class RootTile(cost: Int, val munch: (FlatStmt) -> TileAttempt) : Tile(cost) {
        class MoveTile(cost: Int, munch: (LIRMove) -> TileAttempt) : RootTile(cost, {
                when (it) {
                    is LIRMove -> munch(it)
                    else -> TileAttempt(false)
                }
            })

        class JumpTile(cost: Int, munch: (LIRJump) -> TileAttempt) : RootTile(cost, {
            when (it) {
                is LIRJump -> munch(it)
                else -> TileAttempt(false)
            }
        })

        class CJumpTile(cost: Int, munch: (LIRTrueJump) -> TileAttempt) : RootTile(cost, {
            when (it) {
                is LIRTrueJump -> munch(it)
                else -> TileAttempt(false)
            }
        })

        class ReturnTile(cost: Int, munch: (LIRReturn) -> TileAttempt) : RootTile(cost, {
            when (it) {
                is LIRReturn -> munch(it)
                else -> TileAttempt(false)
            }
        })

        class CallTile(cost: Int, munch: (LIRCallStmt) -> TileAttempt) : RootTile(cost, {
            when (it) {
                is LIRCallStmt -> munch(it)
                else -> TileAttempt(false)
            }
        })
    }


    sealed class ExprTile(cost: Int, val munch: (LIRExpr, Register) -> TileAttempt) : Tile(cost) {

        class OpTile(
            cost: Int,
            munch: (LIROp, Register) -> TileAttempt) : ExprTile(cost, {
            it, r ->
            when (it) {
                is LIROp -> munch(it, r)
                else -> TileAttempt(false)
            }
        })

        class MemTile(
            cost: Int,
            munch: (LIRMem, Register) -> TileAttempt
        ) : ExprTile(cost, {
            it, r ->
            when (it) {
                is LIRMem -> munch(it, r)
                else -> TileAttempt(false)
            }
        })
    }


}