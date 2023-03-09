package ir.mid

import edu.cornell.cs.cs4120.etac.ir.IRFuncDecl as JIRFuncDecl

/** IRFuncDecl represents a function declaration**/
class IRFuncDecl(val name: String, val body: IRStmt) : IRNode() {
    override val java: JIRFuncDecl = factory.IRFuncDecl(name, body.java)
}