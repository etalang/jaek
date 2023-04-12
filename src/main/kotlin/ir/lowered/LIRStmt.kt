package ir.lowered

//import assembly.Tile
import assembly.tile.BuiltTile
import assembly.tile.TileBuilder
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRCJump as JIRCJump
import edu.cornell.cs.cs4120.etac.ir.IRJump as JIRJump
import edu.cornell.cs.cs4120.etac.ir.IRLabel as JIRLabel
import edu.cornell.cs.cs4120.etac.ir.IRStmt as JIRStmt

/** IRStmt represents a statement **/
sealed class LIRStmt : LIRNode.TileableNode<BuiltTile.RegularTile>() {
    abstract override val java: JIRStmt;

    sealed class FlatStmt : LIRStmt()
    sealed class EndBlock : FlatStmt()

    /** IRJump represents a jump to address [address]
     *
     **/
    class LIRJump(val address: LIRExpr.LIRName) : EndBlock() {
        override val java: JIRJump = factory.IRJump(address.java)

        override val defaultTile
            get() =
                BuiltTile.RegularTile(listOf(Instruction.Jump.JMP(Location(Label(address.l, false)))), 1)

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

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class LIRTrueJump(val guard: LIRExpr, val trueBranch: LIRLabel) : EndBlock() {
        override val java: JIRCJump = factory.IRCJump(guard.java, trueBranch.l)

        override val defaultTile: BuiltTile.RegularTile
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

        override fun findBestTile() {}
    }

    /** IRLabel represents giving a name [l] to the next statement **/
    class LIRLabel(val l: String) : FlatStmt() {
        override val java: JIRLabel = factory.IRLabel(l)

        //TODO: no clue what false / true
        override val defaultTile get() = BuiltTile.RegularTile(listOf(Label(l, true)), 0)

        override fun findBestTile() {}
    }

}
