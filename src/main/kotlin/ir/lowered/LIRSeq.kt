package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRSeq

/** IRSeq represents the sequential composition of IR statements in [block]**/
class LIRSeq(val block: List<FlatStmt>) : LIRStmt() {
    override val java: IRSeq = factory.IRSeq(block.map { it.java })

    class BasicBlock(val label: LIRLabel, val ordinary: List<FlatStmt>, val end: EndBlock)
}