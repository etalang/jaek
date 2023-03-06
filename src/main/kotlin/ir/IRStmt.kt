package ir

import edu.cornell.cs.cs4120.etac.ir.IRReturn

sealed class IRStmt : IRNode() {
    sealed class IRMove : IRStmt() { // have subclasses to enforce type safety, but maybe this is bad?
        class IRMoveTemp : IRMove() {

        }

        class IRMoveMem: IRMove() {

        }
    }

    class IRSeq(val block : ArrayList<IRStmt>) : IRStmt() {

    }

    class IRJump(val address : IRExpr) : IRStmt() {

    }

    class IRCJump(val guard : IRExpr, val trueBranch : IRStmt, val falseBranch : IRStmt?) {

    }

    class IRLabel(val l : String) {

    }

    class IRReturn(val valList : ArrayList<IRExpr>) : IRStmt() {

    }

    // LOWERED STMT
    class IRCallStmt() : IRStmt() {

    }

    // Unlowered statement? unclear if this is necessary but the reference implementation has this
    class IRExp : IRStmt() {

    }

}
