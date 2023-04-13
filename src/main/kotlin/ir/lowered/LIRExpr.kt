package ir.lowered

import assembly.Tile
import assembly.x86.Destination
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.Source
import edu.cornell.cs.cs4120.etac.ir.IRConst as JIRConst
import edu.cornell.cs.cs4120.etac.ir.IRExpr as JIRExpr
import edu.cornell.cs.cs4120.etac.ir.IRName as JIRName
import edu.cornell.cs.cs4120.etac.ir.IRTemp as JIRTemp

/** IRExpr represents an expression**/
sealed class LIRExpr : LIRNode.TileableNode<Tile.Expr>() {
    abstract override val java: JIRExpr

    /** IRConst(value) represents an integer constant [value]**/
    class LIRConst(val value: Long) : LIRExpr() {
        override val java: JIRConst = factory.IRConst(value)

        override val defaultTile: Tile.Expr
            get() {
                val temp = Register.Abstract.freshRegister()
                return Tile.Expr(
                    listOf(
                        Instruction.MOV(
                            Destination.RegisterDest(temp),
                            Source.ConstSrc(value)
                        )
                    ), 1, temp
                )
            }

        override fun findBestTile() {}
    }

    /** IRTemp(name) represents a temporary register or value named [name] **/
    class LIRTemp(val name: String) : LIRExpr() {
        override val java: JIRTemp = factory.IRTemp(name)

        override val defaultTile get() = Tile.Expr(listOf(), 0, Register.Abstract(name))
        override fun findBestTile() {}
    }

    /** IRName(l) represents the address of a labeled memory address labeled [l]*/
    class LIRName(val l: String) : LIRExpr() {
        override val java: JIRName = factory.IRName(l)

        override val defaultTile get() = TODO("Not yet implemented")
        override fun findBestTile() {
            TODO("Not yet implemented")
        }
    }
}
