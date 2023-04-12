package ir.lowered

import assembly.tile.BuiltTile
import assembly.tile.TileBuilder
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRMem

/** IRMem(address) evaluates [address] and looks up the memory contents in [address]**/
class LIRMem(val address: LIRExpr) : LIRExpr() {
    override val java: IRMem = factory.IRMem(address.java)

    override val defaultTile: BuiltTile.ExprTile
        get() {
            val builder = TileBuilder.Expr(1, Register.Abstract.freshRegister(), this)
            if (address is LIRName) {
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.MemorySrc(Memory.LabelMem(Label(address.l, false)))
                    )
                )
            } else {
                val addressTile = address.optimalTile()
                builder.consume(addressTile)
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.MemorySrc(Memory.RegisterMem(addressTile.outputRegister, null))
                    )
                )
            }
            return builder.build()
        }

    override fun findBestTile() {}
}