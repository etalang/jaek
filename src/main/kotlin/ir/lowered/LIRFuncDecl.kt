package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRFuncDecl as JIRFuncDecl

/** IRFuncDecl represents a function declaration**/
class LIRFuncDecl(val name: String, var body: LIRSeq) : LIRNode() {
    fun reorderBlocks(freshLabel: () -> String) {
        body = body.blockReordering(freshLabel)
    }

    override val java: JIRFuncDecl get() = factory.IRFuncDecl(name, body.java)
}