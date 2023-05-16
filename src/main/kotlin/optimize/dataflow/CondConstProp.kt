package optimize.dataflow

import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
import ir.optimize.ConstantFolder
import optimize.cfg.CFG
import optimize.cfg.CFGExpr
import optimize.cfg.CFGNode
import optimize.cfg.Edge
import optimize.dataflow.Element.Definition
import optimize.dataflow.Element.Unreachability

class CondConstProp(cfg: CFG) : CFGFlow.Forward<CondConstProp.Info>(cfg), PostProc {
    override val top: Info = Info(Unreachability.Top, mutableMapOf())
    override val name: String = "Conditional Constant Propogation"
    private val mm = cfg.mm

    override fun transition(n: CFGNode, inInfo: Info): Map<Edge, Info> {
        val outInfo = inInfo.copy()
        val allVarsTop = outInfo.varVals.mapValues { Definition.Top }
        val unreachableInfo = Info(Unreachability.Top, allVarsTop.toMutableMap())
        if (n is CFGNode.Start) return mm.successorEdges(n)
            .associateWith { Info(Unreachability.Bottom, inInfo.varVals) } // start must be reachable
        if (inInfo.unreachability == Unreachability.Bottom) { // if reachable
            when (n) {
                is CFGNode.If -> {
                    when (val guardAbs = abstractInterpretation(n.cond, varVals = outInfo.varVals)) {
                        Definition.Bottom -> return mm.successorEdges(n)
                            .associateWith { outInfo } // can't predict anything here
                        is Definition.Data -> {
                            val falseEdge = Edge(n, mm.fallThrough(n)!!, false)
                            val trueEdge = Edge(n, mm.jumpingTo(n)!!, true)
                            val cond = n.cond
                            if (guardAbs.t == 0L) { // false edge TAKEN
                                if (cond is CFGExpr.BOp && cond.op == NEQ && cond.left is CFGExpr.Var) {
                                    // add extra info to map based on condition info
                                    outInfo.varVals[cond.left.name] =
                                        abstractInterpretation(cond.right, varVals = outInfo.varVals)
                                }
                                return mapOf(trueEdge to unreachableInfo, falseEdge to outInfo)
                            } else if (guardAbs.t == 1L) { // true edge TAKEN
                                if (cond is CFGExpr.BOp && cond.op == EQ && cond.left is CFGExpr.Var) {
                                    outInfo.varVals[cond.left.name] =
                                        abstractInterpretation(cond.right, varVals = outInfo.varVals)
                                }
                                return mapOf(trueEdge to outInfo, falseEdge to unreachableInfo)
                            } else throw Exception("guard value is neither 0 nor 1, should not typecheck")
                        }

                        Definition.Top -> { // this means we have not yet processed node (random ordering!!)
                            return mm.successorEdges(n).associateWith { outInfo }
                        }

                        is Definition.DesignatedMeeter -> throw Exception("pls do not meet")
                    }
                }

                is CFGNode.Gets -> {
                    outInfo.varVals[n.varName] = abstractInterpretation(n.expr, varVals = outInfo.varVals)
                    return mm.successorEdges(n).associateWith { outInfo }
                }

                else -> {
                    return mm.successorEdges(n).associateWith { outInfo }
                } // no change
            }

        } else {
            return mm.successorEdges(n).associateWith { unreachableInfo } // we return T, T vec when unreachable
        }
    }

    fun abstractInterpretation(expr: CFGExpr, varVals: MutableMap<String, Definition>): Definition {
        // 2+2 =4, 2+ top = top, 2+bot = bot, f(x)= bot
        // fx doesn't even exist in CFGExpr
        return when (expr) {
            is CFGExpr.BOp -> {
                val leftAbs = abstractInterpretation(expr.left, varVals) // consider whether order matters here
                val rightAbs = abstractInterpretation(expr.right, varVals)
                when (leftAbs) {
                    is Definition.Data -> {
                        when (rightAbs) {
                            is Definition.Data -> {
                                if (expr.op != DIV && rightAbs.t != 0L) {
                                    Definition.Data(ConstantFolder.calculate(leftAbs.t, rightAbs.t, expr.op))
                                } else {
                                    Definition.Bottom // don't do anything and hope for runtime failure
                                }
                            }

                            else -> rightAbs
                        }
                    }

                    else -> leftAbs
                }
            }

            is CFGExpr.Const -> Definition.Data(expr.value)
            is CFGExpr.Label -> Definition.Bottom // globals, not currently doing this
            is CFGExpr.Mem -> Definition.Bottom // not currently saving the memory
            is CFGExpr.Var -> varVals.getOrDefault(expr.name, Definition.Top)
        }
    }

    private final val defMoosher = Definition.DesignatedMeeter().meet
    private final val reachMoosher = Unreachability.DesignatedMeeter().meet
    override fun meet(e1: Info, e2: Info): Info {
        val unreachability = reachMoosher.meet(e1.unreachability, e2.unreachability)

        val defdVars = e1.varVals.keys union e2.varVals.keys
        val e1vals = e1.varVals.withDefault { Definition.Top } // signfies undefd var
        val e2vals = e2.varVals.withDefault { Definition.Top }
        val mapMeet = defdVars.map {
            defMoosher.meet(e1vals.getValue(it), e2vals.getValue(it))
        }
        return Info(unreachability, defdVars.zip(mapMeet).toMap().toMutableMap())
    }

    /** [varVals] must be treated as default T (top) when key not contained */
    data class Info(val unreachability: Unreachability, val varVals: MutableMap<String, Definition>) : EdgeValues() {
        override val pretty: String get() = "($unreachability, ($varVals))"
        fun copy(): Info {
            return Info(unreachability, varVals.toMutableMap())
        }
    }

    override fun postprocess() {
        var unreachablesExist = true
        while (unreachablesExist) {
            println("removing an unreachable")
            unreachablesExist = removeUnreachables()
//            run()
        }
        var lonelyIfsExist = true
        while (lonelyIfsExist) {
            println("removing lonely if")
            lonelyIfsExist = removeLonelyIfs()
//            run()
        }
        run()
        constantPropogate()
//        deleteConstAssigns()
    }

    private fun removeLonelyIfs(): Boolean {
        var changed = false
        mm.fastNodesWithPredecessors().filterIsInstance<CFGNode.If>().forEach {
            val falseEdge = mm.fallThrough(it)
            val trueEdge = mm.jumpingTo(it)
            if (trueEdge == null && falseEdge == null) {
                mm.removeNode(it)
                changed = true
            } else if (falseEdge != null && trueEdge == null) {
                mm.predecessors(it).forEach { pred ->
                    mm.translate(pred, it, falseEdge)
                }
                changed = true
            } else if (trueEdge != null && falseEdge == null) {
                mm.predecessors(it).forEach { pred ->
                    mm.translate(pred, it, trueEdge)
                }
                changed = true
            }
        }
        return changed
    }

    //
//    private fun deleteConstAssigns() {
//        var predEdges = cfg.getPredEdges()
//        cfg.getNodes().forEach { curNode ->
//            if (curNode is CFGNode.Gets && curNode.expr is CFGExpr.Const) {
//                curNode.edges.forEach { outEdge -> // a gets should always only have one
//                    val nodePreds = predEdges.getOrDefault(curNode, emptySet())
//                    nodePreds.forEach { inEdge ->
//                        inEdge.node = outEdge.node // delete gets const node
//                    }
//                    predEdges = cfg.getPredEdges()
////                    File("delet${curNode.pretty.filterNot { it.isWhitespace() }}.dot").writeText(graphViz())
//                }
//            }
//        }
//    }
//
    private fun constantPropogate() {
        mm.fastNodesWithPredecessors().forEach { curNode ->
            val met = bigMeet(mm.predecessorEdges(curNode))
            met.varVals.forEach {
                val value = it.value
                if (value is Definition.Data) {
                    when (curNode) {
                        is CFGNode.Funcking -> {
                            curNode.args = curNode.args.map { arg ->
                                replaceVar(arg, it.key, value.t)
                            }
                        }

                        is CFGNode.If -> curNode.cond = replaceVar(curNode.cond, it.key, value.t)
                        is CFGNode.Gets -> {
                            curNode.expr = replaceVar(curNode.expr, it.key, value.t)
                        }

                        is CFGNode.Mem -> {
                            curNode.loc = replaceVar(curNode.loc, it.key, value.t)
                            curNode.expr = replaceVar(curNode.expr, it.key, value.t)
                        }

                        is CFGNode.Return ->
                            curNode.rets = curNode.rets.map { ret ->
                                replaceVar(ret, it.key, value.t)
                            }

                        is CFGNode.Start, is CFGNode.Cricket -> {}
                    }
                }
            }

        }

    }

    //
    private fun replaceVar(expr: CFGExpr, varName: String, varVal: Long): CFGExpr {
        return when (expr) {
            is CFGExpr.BOp -> CFGExpr.BOp(
                replaceVar(expr.left, varName, varVal),
                replaceVar(expr.right, varName, varVal),
                expr.op
            )

            is CFGExpr.Const -> expr
            is CFGExpr.Label -> expr
            is CFGExpr.Mem -> CFGExpr.Mem(replaceVar(expr.loc, varName, varVal))
            is CFGExpr.Var -> if (expr.name == varName) CFGExpr.Const(varVal) else expr
        }
    }

    /* returns false when no change */
    private fun removeUnreachables(): Boolean {
        val remove = mm.fastNodesWithPredecessors().firstOrNull {
            it !is CFGNode.Start && bigMeet(mm.predecessorEdges(it)).unreachability is Unreachability.Top
        };
        if (remove != null) {
            mm.removeNode(remove)
            return true
        }
        return false
    }
}