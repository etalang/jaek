package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRMem

/** IRMem(address) evaluates [address] and looks up the memory contents in [address]**/
class LIRMem(val address: LIRExpr) : LIRExpr() {
    override val java: IRMem = factory.IRMem(address.java)

    override val defaultTile get() = TODO("Not yet implemented")
    override fun findBestTile() { TODO("Not yet implemented") }
}