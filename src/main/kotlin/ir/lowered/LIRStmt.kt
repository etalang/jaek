package ir.lowered

//import assembly.Tile
import assembly.Tile
import assembly.TileBuilder
import assembly.x86.*
import assembly.x86.Destination.*
import assembly.x86.Source.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRCJump as JIRCJump
import edu.cornell.cs.cs4120.etac.ir.IRJump as JIRJump
import edu.cornell.cs.cs4120.etac.ir.IRLabel as JIRLabel
import edu.cornell.cs.cs4120.etac.ir.IRStmt as JIRStmt

/** IRStmt represents a statement **/
sealed class LIRStmt : LIRNode.TileableNode<Tile.Regular>() {
    abstract override val java: JIRStmt

    sealed class FlatStmt : LIRStmt()
    sealed class EndBlock : FlatStmt()

    /** IRJump represents a jump to address [address]
     *
     **/
    class LIRJump(val address: LIRExpr.LIRName) : EndBlock() {
        override val java: JIRJump = factory.IRJump(address.java)

        override val defaultTile
            get() =
                Tile.Regular(listOf(Instruction.Jump.JMP(Location(Label(address.l, false)))), 1)

        override fun findBestTile() {}
    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class LIRCJump(val guard: LIRExpr, val trueBranch: LIRLabel, val falseBranch: LIRLabel?) :
        EndBlock() {
        override val java: JIRCJump =
        //WE SHOULDN'T EVER CALL THIS : UNSUPPORTED OPERATION
            //(WHEN WE CALL THIS falseBranch SHOULD BE NULL!!!!!!) THUS IT SHOULD BE LIRTrueJump
            if (falseBranch != null) factory.IRCJump(guard.java, trueBranch.l, falseBranch.l)
            else factory.IRCJump(guard.java, trueBranch.l)

        override val defaultTile get() = throw Exception("can't tile non canonical")

        override fun findBestTile() {}

    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero**/
    class LIRTrueJump(val guard: LIRExpr, val trueBranch: LIRLabel) : EndBlock() {
        override val java: JIRCJump = factory.IRCJump(guard.java, trueBranch.l)

        override val defaultTile: Tile.Regular
            get() {
                val builder = TileBuilder.Regular(2, this)
                val guardTile = guard.optimalTile()
                builder.consume(guardTile)
                builder.add(
                    listOf(
                        Instruction.TEST(guardTile.outputRegister, guardTile.outputRegister),
                        Instruction.Jump.JNZ(Location(Label(trueBranch.l, false)))
                    )
                )
                return builder.build()
            }

        override fun findBestTile() {
            attempt(exprGuard())
            attempt(zeroNotZero())
            attempt(alwaysTrue())
            attempt(alwaysFalse())
        }

        private fun exprGuard() : Tile.Regular? {
            if (guard is LIROp) {
                val builder = TileBuilder.Regular(2, this)
                val leftTile = guard.left.optimalTile()
                val rightTile = guard.right.optimalTile()
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(Instruction.CMP(RegisterDest(leftTile.outputRegister), RegisterSrc(rightTile.outputRegister)))
                when (guard.op) {
                    IRBinOp.OpType.EQ -> {
                        builder.add(Instruction.Jump.JE(Location(Label(trueBranch.l, false))))
                    }
                    IRBinOp.OpType.NEQ -> {
                        builder.add(Instruction.Jump.JNE(Location(Label(trueBranch.l, false))))
                    }
                    IRBinOp.OpType.LT -> {
                        builder.add(Instruction.Jump.JL(Location(Label(trueBranch.l, false))))
                    }
                    IRBinOp.OpType.ULT -> {
                        builder.add(Instruction.Jump.JB(Location(Label(trueBranch.l, false))))
                    }
                    IRBinOp.OpType.GT -> {
                        builder.add(Instruction.Jump.JG(Location(Label(trueBranch.l, false))))
                    }
                    IRBinOp.OpType.LEQ -> {
                        builder.add(Instruction.Jump.JLE(Location(Label(trueBranch.l, false))))
                    }
                    IRBinOp.OpType.GEQ -> {
                        builder.add(Instruction.Jump.JGE(Location(Label(trueBranch.l, false))))
                    }
                    else -> return null
                }
                return builder.build()
            }
            return null
        }

        /* TODO: this needs a refactor LMAO */
        private fun zeroNotZero() : Tile.Regular? {
            if (guard is LIROp) {
                val builder = TileBuilder.Regular(2, this)
                if (guard.op == IRBinOp.OpType.EQ) {
                    if (guard.left is LIRExpr.LIRConst && guard.left.value == 0L) {
                        val rightTile = guard.right.optimalTile()
                        builder.consume(rightTile)
                        builder.add(Instruction.CMP(RegisterDest(rightTile.outputRegister), ConstSrc(0L)))
                        builder.add(Instruction.Jump.JZ(Location(Label(trueBranch.l, false))))
                        return builder.build()
                    }
                    else if (guard.right is LIRExpr.LIRConst && guard.right.value == 0L) {
                        val leftTile = guard.left.optimalTile()
                        builder.consume(leftTile)
                        builder.add(Instruction.CMP(RegisterDest(leftTile.outputRegister), ConstSrc(0L)))
                        builder.add(Instruction.Jump.JZ(Location(Label(trueBranch.l, false))))
                        return builder.build()
                    }
                }
                else if (guard.op == IRBinOp.OpType.NEQ) {
                    if (guard.left is LIRExpr.LIRConst && guard.left.value == 0L) {
                        val rightTile = guard.right.optimalTile()
                        builder.consume(rightTile)
                        builder.add(Instruction.CMP(RegisterDest(rightTile.outputRegister), ConstSrc(0L)))
                        builder.add(Instruction.Jump.JNZ(Location(Label(trueBranch.l, false))))
                        return builder.build()
                    }
                    else if (guard.right is LIRExpr.LIRConst && guard.right.value == 0L) {
                        val leftTile = guard.left.optimalTile()
                        builder.consume(leftTile)
                        builder.add(Instruction.CMP(RegisterDest(leftTile.outputRegister), ConstSrc(0L)))
                        builder.add(Instruction.Jump.JNZ(Location(Label(trueBranch.l, false))))
                        return builder.build()
                    }
                }
                else {
                    return null
                }
            }
            return null
        }

        private fun alwaysTrue() : Tile.Regular? {
            if (guard is LIRExpr.LIRConst && guard.value == 1L) {
                val builder = TileBuilder.Regular(1, this)
                builder.add(Instruction.Jump.JMP(Location(Label(trueBranch.l, false))))
                return builder.build()
            }
            return null
        }

        private fun alwaysFalse() : Tile.Regular?{
            if (guard is LIRExpr.LIRConst && guard.value == 0L) {
                val builder = TileBuilder.Regular(0, this)
                return builder.build()
            }
            return null
        }


    }

    /** IRLabel represents giving a name [l] to the next statement **/
    class LIRLabel(val l: String) : FlatStmt() {
        override val java: JIRLabel = factory.IRLabel(l)

        //TODO: no clue what false / true
        override val defaultTile get() = Tile.Regular(listOf(Label(l, true)), 0)

        override fun findBestTile() {}

        override fun toString(): String {
            return "FUCKINGLABEL: $l"
        }
    }

}
