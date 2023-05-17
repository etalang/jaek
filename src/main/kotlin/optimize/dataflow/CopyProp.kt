package optimize.dataflow

import optimize.cfg.CFG
import optimize.cfg.CFGExpr
import optimize.cfg.CFGNode
import optimize.cfg.Edge

class CopyProp(cfg: CFG) : CFGFlow.Forward<CopyProp.Info>(cfg), PostProc, Properties.Def, Properties.Use {
    override val name: String = "Copy Propagation"
    private val mm = cfg.mm

    private fun allVars(): Set<String> {
        val allVars = mutableSetOf<String>()
        for (n in mm.allNodes()) {
            allVars.addAll(use(n))
            allVars.addAll(def(n))
        }
        return allVars
    }

    private fun computeTop(): Set<Copy> {
        val allPairs = mutableSetOf<Copy>()
        val vars = allVars()
        for (v1 in vars) {
            for (v2 in vars) {
                if (v1 != v2) {
                    allPairs.add(Copy(v1, v2))
                }
            }
        }
        return allPairs
    }

    override val top: Info = Info(computeTop())

    override fun meet(e1: Info, e2: Info): Info {
        return Info(e1.copies intersect e2.copies)
    }

    private fun kill(n: CFGNode): Set<Copy> {
        if (n is CFGNode.Gets) {
            val allVars = allVars()
            val killSet = mutableSetOf<Copy>()
            for (v in allVars) {
                killSet.add(Copy(n.varName, v))
                killSet.add(Copy(v, n.varName))
            }
        } else if (n is CFGNode.Start) {
            return top.copies
        }
        return emptySet()
    }

    private fun gen(n: CFGNode): Set<Copy> {
        if (n is CFGNode.Gets) {
            val expr = n.expr
            if (expr is CFGExpr.Var) {
                return setOf(Copy(n.varName, expr.name))
            }
        }
        return emptySet()
    }

    override fun transition(n: CFGNode, argumentInfo: Info): Map<Edge, Info> {
        return mm.successorEdges(n).associateWith {
            Info(argumentInfo.copies.minus(kill(n)) union gen(n))
        }
    }

    data class Copy(val late: String, val early: String) {
        override fun toString(): String {
            return "$late = $early"
        }
    }

    data class Info(val copies: Set<Copy>) : EdgeValues() {
        override val pretty: String get() = copies.toString()
    }

    override fun postprocess() {
        var propagateCopies = true
        while (propagateCopies) {
            propagateCopies = propagateEqualVars()
            run()
            mm.repOk()
        }
    }

    /** idk what to do here */
    private fun propagateEqualVars(): Boolean {
        return false
    }

}