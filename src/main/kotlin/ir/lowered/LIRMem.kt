package ir.lowered

import assembly.Tile
import assembly.TileBuilder
import assembly.x86.*
import assembly.x86.Destination.*
import assembly.x86.Source.*
import edu.cornell.cs.cs4120.etac.ir.IRMem

/** IRMem(address) evaluates [address] and looks up the memory contents in [address]**/
class LIRMem(val address: LIRExpr) : LIRExpr() {
    override val java: IRMem = factory.IRMem(address.java)

    override val defaultTile: Tile.Expr
        get() {
            val builder = TileBuilder.Expr(1, Register.Abstract.freshRegister(), this)
            if (address is LIRName) {
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        MemorySrc(Memory.LabelMem(Label(address.l, false)))
                    )
                )
            } else {
                val addressTile = address.optimalTile()
                builder.consume(addressTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        MemorySrc(Memory.RegisterMem(addressTile.outputRegister, null))
                    )
                )
            }
            return builder.build()
        }

    private fun arrAccessTile(): Tile.Expr? {
        val builder = TileBuilder.Expr(1, Register.Abstract.freshRegister(), this)
        val smartAccess = detectMemoryFriendly(this.address)
        if (smartAccess != null) {
            builder.add(Instruction.MOV(RegisterDest(builder.outputRegister), MemorySrc(smartAccess)))
            return builder.build()
        }
        return null
    }

    override fun findBestTile() {
        attempt(arrAccessTile())
    }
}