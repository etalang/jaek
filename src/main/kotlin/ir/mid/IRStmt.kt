package ir.mid

import edu.cornell.cs.cs4120.etac.ir.IRCJump as JIRCJump
import edu.cornell.cs.cs4120.etac.ir.IRExp as JIRExp
import edu.cornell.cs.cs4120.etac.ir.IRJump as JIRJump
import edu.cornell.cs.cs4120.etac.ir.IRLabel as JIRLabel
import edu.cornell.cs.cs4120.etac.ir.IRMove as JIRMove
import edu.cornell.cs.cs4120.etac.ir.IRReturn as JIRReturn
import edu.cornell.cs.cs4120.etac.ir.IRSeq as JIRSeq
import edu.cornell.cs.cs4120.etac.ir.IRStmt as JIRStmt

/** IRStmt represents a statement **/
sealed class IRStmt : IRNode() {
    override abstract val java: JIRStmt;

    /** IRMove represents moving the result of an expression to a destination**/
    class IRMove(val dest: IRExpr, val expr: IRExpr) : IRStmt() {
        override val java: JIRMove = factory.IRMove(dest.java, expr.java)
    }

    /** IRSeq represents the sequential composition of IR statements in [block]**/
    class IRSeq(val block: List<IRStmt>) : IRStmt() {
        override val java: JIRSeq = factory.IRSeq(block.map { it.java })
    }

    /** IRJump represents a jump to address [address] **/
    class IRJump(val address: IRExpr) : IRStmt() {
        override val java: JIRJump = factory.IRJump(address.java)

    }

    //TODO
    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class IRCJump(val guard: IRExpr, val trueBranch: IRLabel, val falseBranch: IRLabel?) : IRStmt() {
        override val java: JIRCJump =
            if (falseBranch != null)
                factory.IRCJump(guard.java, trueBranch.l, falseBranch.l)
            else
                factory.IRCJump(guard.java, trueBranch.l)

    }

    /** IRLabel represents giving a name [l] to the next statement **/
    class IRLabel(val l: String) : IRStmt() {
        override val java: JIRLabel = factory.IRLabel(l)

    }

    /** IRReturn represents returning 0 or more values in [valList] from the current function **/
    class IRReturn(val valList: List<IRExpr>) : IRStmt() {
        override val java: JIRReturn = factory.IRReturn(valList.map { it.java })

    }

//    // LOWERED STMT
//    class IRCallStmt() : IRStmt() {
//
//    }
//
    class IRExp(val expr: IRExpr) : IRStmt() {
        override val java: JIRExp = factory.IRExp(expr.java)

    }
}
