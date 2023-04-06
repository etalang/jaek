package ir.optimize

import ir.lowered.*

sealed class IROptimizer {
    open fun apply(node: LIRCompUnit): LIRCompUnit {
        return LIRCompUnit(node.name, node.functions.map { applyFuncDecl(it) }, node.globals)
    }

    protected open fun applyFuncDecl(node: LIRFuncDecl): LIRFuncDecl {
        return LIRFuncDecl(node.name, applySeq(node.body))
    }

    protected open fun applySeq(node: LIRSeq): LIRSeq {
        return LIRSeq(node.block.map { applyFlatStmt(it) })
    }

    protected open fun applyFlatStmt(node: LIRStmt.FlatStmt): LIRStmt.FlatStmt {
        return when (node) {
            is LIRStmt.LIRCJump -> LIRStmt.LIRCJump(applyExpr(node.guard), applyLabel(node.trueBranch),
                node.falseBranch?.let { applyLabel(it) })

            is LIRStmt.LIRJump -> LIRStmt.LIRJump(applyExpr(node.address) as LIRExpr.LIRName) // you didn't see anything
            is LIRStmt.LIRReturn -> LIRStmt.LIRReturn(node.valList.map { applyExpr(it) })
            is LIRStmt.LIRTrueJump -> LIRStmt.LIRTrueJump(applyExpr(node.guard), applyLabel(node.trueBranch))
            is LIRStmt.LIRCallStmt -> LIRStmt.LIRCallStmt(applyExpr(node.target),
                node.n_returns,
                node.args.map { applyExpr(it) })

            is LIRStmt.LIRLabel -> applyLabel(node)
            is LIRStmt.LIRMove -> LIRStmt.LIRMove(applyExpr(node.dest), applyExpr(node.expr))
        }
    }

    protected open fun applyLabel(node: LIRStmt.LIRLabel): LIRStmt.LIRLabel {
        return node;
    }

    protected open fun applyExpr(node: LIRExpr): LIRExpr {
        return when (node) {
            is LIRExpr.LIRConst -> node
            is LIRExpr.LIRMem -> applyMem(node)
            is LIRExpr.LIRName -> applyName(node)
            is LIRExpr.LIROp -> applyOp(node)
            is LIRExpr.LIRTemp -> applyTemp(node)
        }
    }

    protected open fun applyTemp(node: LIRExpr.LIRTemp): LIRExpr {
        return node
    }

    protected open fun applyOp(node: LIRExpr.LIROp): LIRExpr {
        return LIRExpr.LIROp(node.op, applyExpr(node.left), applyExpr(node.right))
    }

    protected open fun applyMem(node: LIRExpr.LIRMem): LIRExpr {
        return LIRExpr.LIRMem(applyExpr(node.address))
    }

    protected open fun applyName(node: LIRExpr.LIRName): LIRExpr {
        return node
    }
}
