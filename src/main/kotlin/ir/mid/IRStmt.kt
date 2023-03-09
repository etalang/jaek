package ir.mid

import edu.cornell.cs.cs4120.etac.ir.IRStmt as JIRStmt
import edu.cornell.cs.cs4120.etac.ir.IRMove as JIRMove
import edu.cornell.cs.cs4120.etac.ir.IRCJump as JIRCJump
import edu.cornell.cs.cs4120.etac.ir.IRJump as JIRJump
import edu.cornell.cs.cs4120.etac.ir.IRLabel as JIRLabel
import edu.cornell.cs.cs4120.etac.ir.IRReturn as JIRReturn
import edu.cornell.cs.cs4120.etac.ir.IRSeq as JIRSeq

/** IRStmt represents a statement **/
sealed class IRStmt : IRNode() {
    override abstract val java: JIRStmt;
            /** IRMove represents moving the result of an expression to a destination**/
     class IRMove(val dest: IRExpr, val expr: IRExpr) : IRStmt() {
        override val java: JIRMove = TODO("Not yet implemented")
    }

    /** IRSeq represents the sequential composition of IR statements in [block]**/
    class IRSeq(val block: ArrayList<IRStmt>) : IRStmt() {
        override val java: JIRSeq = TODO("Not yet implemented")
    }

    /** IRJump represents a jump to address [address] **/
    class IRJump(val address: IRExpr) : IRStmt() {
        override val java: JIRJump = TODO("Not yet implemented")

    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class IRCJump(val guard: IRExpr, val trueBranch: IRStmt, val falseBranch: IRStmt?) : IRStmt() {
        override val java: JIRCJump = TODO("Not yet implemented")

    }

    /** IRLabel represents giving a name [l] to the next statement **/
    class IRLabel(val l: String) : IRStmt() {
        override val java: JIRLabel = factory.IRLabel(l)

    }

    /** IRReturn represents returning 0 or more values in [valList] from the current function **/
    class IRReturn(val valList: ArrayList<IRExpr>) : IRStmt() {
        override val java: JIRReturn = TODO("Not yet implemented")

    }

//    // LOWERED STMT
//    class IRCallStmt() : IRStmt() {
//
//    }
//
//    // Unlowered statement? unclear if this is necessary but the reference implementation has this
//    class IRExp : IRStmt() {
//
//    }
}
