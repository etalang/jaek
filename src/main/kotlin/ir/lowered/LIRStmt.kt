package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRCJump as JIRCJump
import edu.cornell.cs.cs4120.etac.ir.IRCallStmt as JIRCallStmt
import edu.cornell.cs.cs4120.etac.ir.IRJump as JIRJump
import edu.cornell.cs.cs4120.etac.ir.IRLabel as JIRLabel
import edu.cornell.cs.cs4120.etac.ir.IRMove as JIRMove
import edu.cornell.cs.cs4120.etac.ir.IRReturn as JIRReturn
import edu.cornell.cs.cs4120.etac.ir.IRSeq as JIRSeq
import edu.cornell.cs.cs4120.etac.ir.IRStmt as JIRStmt

/** IRStmt represents a statement **/
sealed class LIRStmt : LIRNode() {
    sealed class LIRFlatStmt : LIRStmt()

    override abstract val java: JIRStmt;


    /** IRMove represents moving the result of an expression to a destination**/
    class LIRMove(val dest: LIRExpr, val expr: LIRExpr) : LIRFlatStmt() {
        override val java: JIRMove = factory.IRMove(dest.java, expr.java)
    }

    /** IRSeq represents the sequential composition of IR statements in [block]**/
    class LIRSeq(val block: List<LIRFlatStmt>) : LIRStmt() {
        override val java: JIRSeq = factory.IRSeq(block.map { it.java })
    }

    /** IRJump represents a jump to address [address] **/
    class LIRJump(val address: LIRExpr) : LIRFlatStmt() {
        override val java: JIRJump = factory.IRJump(address.java)

    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class IRCJump(val guard: LIRExpr, val trueBranch: LIRLabel, val falseBranch: LIRLabel?) : LIRFlatStmt() {
        override val java: JIRCJump =
            if (falseBranch != null)
                factory.IRCJump(guard.java, trueBranch.l, falseBranch.l)
            else
                factory.IRCJump(guard.java, trueBranch.l)

    }

    /** IRLabel represents giving a name [l] to the next statement **/
    class LIRLabel(val l: String) : LIRFlatStmt() {
        override val java: JIRLabel = factory.IRLabel(l)

    }

    /** IRReturn represents returning 0 or more values in [valList] from the current function **/
    class LIRReturn(val valList: List<LIRExpr>) : LIRFlatStmt() {
        override val java: JIRReturn = factory.IRReturn(valList.map { it.java })

    }

    //TODO
    class LIRCallStmt(val target: LIRExpr, val args: List<LIRExpr>) : LIRFlatStmt() {
        override val java: JIRCallStmt = factory.IRCallStmt(target.java, args.size.toLong(), args.map { it.java })
    }

}
