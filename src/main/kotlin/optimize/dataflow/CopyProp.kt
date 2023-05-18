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
        if (n is CFGNode.Start) {
            return top.copies
        } else{
            val allVars = allVars()
            val killSet = mutableSetOf<Copy>()
            for (kill in def(n))
                for (v in allVars) {
                    killSet.add(Copy(kill, v))
                    killSet.add(Copy(v, kill))
                }
            return killSet
        }
//        return emptySet()
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
        var propagateCopies = propagateEqualVars()
        while (propagateCopies.isNotEmpty()) {
            mm.repOk()
            propagateCopies = propagateEqualVars()
            runWithWorklist(propagateCopies)
        }
    }

    private fun propagateEqualVars(): Set<CFGNode> {
        val changed = mutableSetOf<CFGNode>()
        mm.fastNodesWithPredecessors().forEach { curNode ->
            val met = bigMeet(mm.predecessorEdges(curNode))
            when (curNode) {
                is CFGNode.Funcking -> {
                    var changeTest = false
                    curNode.args = curNode.args.map { arg ->
                        val (e, c) = replaceVar(arg, met)
                        changeTest = changeTest || c
                        e
                    }
                    if (changeTest) changed.add(curNode)
                }

                is CFGNode.If -> {
                    val (e, c) = replaceVar(curNode.cond, met)
                    curNode.cond = e
                    if (c) changed.add(curNode)
                }

                is CFGNode.Gets -> {
                    val (e, c) = replaceVar(curNode.expr, met)
                    curNode.expr = e
                    if (c) changed.add(curNode)
                }

                is CFGNode.Mem -> {
                    val (el, cl) = replaceVar(curNode.loc, met)
                    val (ee, ce) = replaceVar(curNode.expr, met)
                    curNode.loc = el
                    curNode.expr = ee
                    if (cl || ce) changed.add(curNode)
                }

                is CFGNode.Return -> {
                    var changeTest = false

                    val old = curNode.rets
                    curNode.rets = curNode.rets.map { ret ->
                        val (e, c) = replaceVar(ret, met)
                        changeTest = changeTest || c
                        e
                    }
                    if (changeTest) changed.add(curNode)
                }

                is CFGNode.Start, is CFGNode.Cricket, is CFGNode.NOOP -> {}
            }
        }
        return changed
    }

    private fun replaceVar(expr: CFGExpr, pairs: CopyProp.Info): Pair<CFGExpr, Boolean> {
        return when (expr) {
            is CFGExpr.BOp -> {
                val (leftE, leftC) = replaceVar(expr.left, pairs)
                val (rightE, rightC) = replaceVar(expr.right, pairs)
                Pair(
                    CFGExpr.BOp(
                        leftE, rightE, expr.op
                    ), leftC || rightC
                )
            }

            is CFGExpr.Const -> Pair(expr, false)
            is CFGExpr.Label -> Pair(expr, false)
            is CFGExpr.Mem -> {
                val (e, changed) = replaceVar(expr.loc, pairs)
                Pair(CFGExpr.Mem(e), changed)
            }

            is CFGExpr.Var -> {
                val earliest = pairs.copies.find { it.late == expr.name }?.early
                if (earliest != null && earliest != expr.name) {
                    Pair(CFGExpr.Var(earliest), true)
                } else Pair(expr, false)
            }
        }
    }

//    fun earliest(late: String, info: CopyProp.Info): String? {
//        val earlier = info.copies.find { it.late == late }?.early
//        if (earlier != null) {
//            val tryAgain = earliest(earlier, Info(info.copies.minus(Copy(late, earlier))))
//            if (tryAgain != null) return tryAgain
//        }
//        println("earliest for $late is $earlier")
//        return earlier
//    }
}