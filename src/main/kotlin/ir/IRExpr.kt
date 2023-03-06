package ir

sealed class IRExpr : IRNode() {
    class IRConst(val value : Long) : IRExpr() {

    }

    class IRTemp(val name : String) : IRExpr() {

    }
    class IRBinop(val left : IRExpr, val right : IRExpr) : IRExpr() {

    }

    class IRMem(val address: IRExpr) : IRExpr() {

    }

    // NOT LOWERED
    class IRCall(val args: ArrayList<IRExpr>): IRExpr() {

    }

    class IRName(val l : String) : IRExpr() {

    }

    // NOT LOWERED
    class IRESeq(val sideEffect : IRStmt, val value : IRExpr) : IRExpr() {

    }
}
