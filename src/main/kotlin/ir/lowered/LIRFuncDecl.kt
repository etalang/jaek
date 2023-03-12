package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRFuncDecl as JIRFuncDecl

/** IRFuncDecl represents a function declaration**/
class LIRFuncDecl(val name: String, val body: LIRStmt) : LIRNode() {
    override val java: JIRFuncDecl = factory.IRFuncDecl(name, body.java)
}