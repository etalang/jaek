package optimize.dataflow

import optimize.cfg.CFGExpr
import optimize.cfg.CFGNode

sealed interface Properties {
    interface Use : Properties {
        fun use(n: CFGNode): Set<String> {
            return when (n) {
                is CFGNode.Cricket -> emptySet()
                is CFGNode.NOOP -> emptySet()
                is CFGNode.Funcking -> (n.args.fold<CFGExpr, Set<String>>(emptySet()) { acc, it -> acc union exprUse(it) }
                        union (n.movIntos.fold(emptySet()) { acc, it -> acc union moveUse(it) }))

                is CFGNode.If -> exprUse(n.cond)
                is CFGNode.Gets -> exprUse(n.expr)
                is CFGNode.Mem -> exprUse(n.expr) union exprUse(n.loc)
                is CFGNode.Return -> n.rets.fold(emptySet()) { acc, it -> acc union exprUse(it) }
                is CFGNode.Start -> emptySet()
            }
        }

        fun exprUse(expr: CFGExpr): Set<String> {
            return when (expr) {
                is CFGExpr.BOp -> exprUse(expr.left) union exprUse(expr.right)
                is CFGExpr.Const -> emptySet()
                is CFGExpr.Label -> emptySet()
                is CFGExpr.Mem -> exprUse(expr.loc)
                is CFGExpr.Var -> setOf(expr.name)
            }
        }

        fun moveUse(mov: CFGNode.Mov): Set<String> {
            return when (mov) {
                is CFGNode.Gets -> setOf(mov.varName) union exprUse(mov.expr)
                is CFGNode.Mem -> exprUse(mov.loc) union exprUse(mov.expr)
            }
        }
    }

    interface Def : Properties {
        fun def(n: CFGNode): Set<String> {
            return when (n) {
                is CFGNode.Funcking -> n.movIntos.fold(emptySet()) { acc, it -> acc union moveDef(it) }
                is CFGNode.Gets -> setOf(n.varName)
                else -> {
                    emptySet()
                }
            }
        }

        fun moveDef(mov: CFGNode.Mov): Set<String> {
            return when (mov) {
                is CFGNode.Gets -> setOf(mov.varName)
                is CFGNode.Mem -> emptySet()
            }
        }
    }
}