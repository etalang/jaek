package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRBinOp

/** IROp(left,right) represents the evaluation of an arithmetic, logical, or relational
 * operation on the evaluated expressions of [left] and [right]**/
class LIROp(val op: IRBinOp.OpType, val left: LIRExpr, val right: LIRExpr) : LIRExpr() {
    override val java: IRBinOp = factory.IRBinOp(op, left.java, right.java)

    override val defaultTile get() = TODO("Not yet implemented")
    override fun findBestTile() { TODO("Not yet implemented") }
}