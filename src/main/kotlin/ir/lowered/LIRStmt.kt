package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRCJump as JIRCJump
import edu.cornell.cs.cs4120.etac.ir.IRCallStmt as JIRCallStmt
import edu.cornell.cs.cs4120.etac.ir.IRJump as JIRJump
import edu.cornell.cs.cs4120.etac.ir.IRLabel as JIRLabel
import edu.cornell.cs.cs4120.etac.ir.IRMove as JIRMove
import edu.cornell.cs.cs4120.etac.ir.IRReturn as JIRReturn
import edu.cornell.cs.cs4120.etac.ir.IRStmt as JIRStmt

/** IRStmt represents a statement **/
sealed class LIRStmt : LIRNode() {
    override abstract val java: JIRStmt;

    sealed class FlatStmt : LIRStmt()
    sealed class EndBlock : FlatStmt()

    /** IRMove represents moving the result of an expression to a destination**/
    class LIRMove(val dest: LIRExpr, val expr: LIRExpr) : FlatStmt() {
        override val java: JIRMove = factory.IRMove(dest.java, expr.java)
    }

    /** IRJump represents a jump to address [address]
     *
     * IMPORTANT INVARIANT: ANY INSTANCES OF [LIRExpr.LIRName] MUST BE IMMEDIATELY IN [address]
     * **/
    class LIRJump(val address: LIRExpr.LIRName) : EndBlock() {
        override val java: JIRJump = factory.IRJump(address.java)
    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class LIRCJump(val guard: LIRExpr, val trueBranch: LIRLabel, val falseBranch: LIRLabel?) : EndBlock() {
        override val java: JIRCJump =
            //WE SHOULDN'T EVER CALL THIS : UNSUPPORTED OPERATION
            //(WHEN WE CALL THIS falseBranch SHOULD BE NULL!!!!!!) THUS IT SHOULD BE LIRTrueJump
            if (falseBranch != null)
                factory.IRCJump(guard.java, trueBranch.l, falseBranch.l)
            else
                factory.IRCJump(guard.java, trueBranch.l)
    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class LIRTrueJump(val guard: LIRExpr, val trueBranch: LIRLabel) : EndBlock() {
        override val java: JIRCJump =
                factory.IRCJump(guard.java, trueBranch.l)
    }

    /** IRLabel represents giving a name [l] to the next statement **/
    class LIRLabel(val l: String) : FlatStmt() {
        override val java: JIRLabel = factory.IRLabel(l)

    }

    /** IRReturn represents returning 0 or more values in [valList] from the current function **/
    class LIRReturn(val valList: List<LIRExpr>) : EndBlock() {
        override val java: JIRReturn = factory.IRReturn(valList.map { it.java })
    }

    class LIRCallStmt(val target: LIRExpr.LIRName, val n_returns : Long, val args: List<LIRExpr>) : FlatStmt() {
        override val java: JIRCallStmt = factory.IRCallStmt(target.java, n_returns, args.map { it.java })
    }

}
