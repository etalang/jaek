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
            is LIRStmt.LIRCJump -> LIRStmt.LIRCJump(
                applyExpr(node.guard),
                applyLabel(node.trueBranch),
                node.falseBranch?.let { applyLabel(it) })

            is LIRStmt.LIRJump -> LIRStmt.LIRJump(node.address)
            is LIRReturn -> LIRReturn(node.valList.map { applyExpr(it) })
            is LIRStmt.LIRTrueJump -> LIRStmt.LIRTrueJump(applyExpr(node.guard), applyLabel(node.trueBranch))
            is LIRCallStmt -> {
                LIRCallStmt(node.target, node.n_returns, node.args.map { applyExpr(it) })
            }

            is LIRStmt.LIRLabel -> applyLabel(node)
            is LIRMove -> LIRMove(applyExpr(node.dest), applyExpr(node.expr))
        }
    }

    protected open fun applyLabel(node: LIRStmt.LIRLabel): LIRStmt.LIRLabel {
        return node
    }

    protected open fun applyExpr(node: LIRExpr): LIRExpr {
        return when (node) {
            is LIRExpr.LIRConst -> node
            is LIRMem -> applyMem(node)
            is LIRExpr.LIRName -> node
            is LIROp -> applyOp(node)
            is LIRExpr.LIRTemp -> applyTemp(node)
        }
    }

    protected open fun applyTemp(node: LIRExpr.LIRTemp): LIRExpr {
        return node
    }

    protected open fun applyOp(node: LIROp): LIRExpr {
        return LIROp(node.op, applyExpr(node.left), applyExpr(node.right))
    }

    protected open fun applyMem(node: LIRMem): LIRExpr {
        return LIRMem(applyExpr(node.address))
    }
}
