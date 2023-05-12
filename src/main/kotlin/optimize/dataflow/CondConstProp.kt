package optimize.dataflow

import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
import ir.lowered.LIRExpr
import ir.optimize.ConstantFolder
import optimize.cfg.CFG
import optimize.cfg.CFGExpr
import optimize.cfg.CFGNode
import optimize.cfg.Edge
import optimize.dataflow.Element.*
import optimize.dataflow.Properties.*

class CondConstProp(cfg: CFG) : CFGFlow.Forward<CondConstProp.Info>(cfg), Def {
    override val top: Info = Info(Unreachability.Bottom, mutableMapOf())
    override val name: String = "Conditional Constant Propogation"

    override fun transition(n: CFGNode, inInfo: Info): Map<Edge, Info> {
        val outInfo = inInfo.copy()
        val allVarsTop = outInfo.varVals.mapValues { Definition.Top }
        val unreachableInfo = Info(Unreachability.Top, allVarsTop.toMutableMap())
        if (inInfo.unreachability == Unreachability.Bottom) { // if reachable
            when (n) {
                is CFGNode.If -> {
                    when (val guardAbs = abstractInterpretation(n.cond, varVals = outInfo.varVals)) {
                        Definition.Bottom -> return n.edges.associateWith { outInfo } // can't predict anything here
                        is Definition.Data -> {
//                            val first = n.edges.first()
//                            val second = n.edges.last() // assuming only two edges out of if
                            val falseEdge = n.to
                            val trueEdge = n.take
                            if (guardAbs.t == 0L) { // false edge TAKEN
                                if (n.cond is CFGExpr.BOp && n.cond.op == NEQ && n.cond.left is CFGExpr.Var) {
                                    // add extra info to map based on condition info
                                    outInfo.varVals[n.cond.left.name] = abstractInterpretation(n.cond.right, varVals = outInfo.varVals)
                                }
                                falseEdge?.let {
                                    trueEdge?.let{
                                        return mapOf(trueEdge to unreachableInfo, falseEdge to outInfo)
                                    }
                                    return mapOf(falseEdge to outInfo)
                                }
                                trueEdge?.let{return mapOf(trueEdge to unreachableInfo)}
                                return mapOf()
                            } else if (guardAbs.t == 1L) { // true edge TAKEN
                                if (n.cond is CFGExpr.BOp && n.cond.op == EQ && n.cond.left is CFGExpr.Var) {
                                    // add extra info to map based on condition info
                                    outInfo.varVals[n.cond.left.name] = abstractInterpretation(n.cond.right, varVals = outInfo.varVals)
                                }
                                falseEdge?.let {
                                    trueEdge?.let{
                                        return mapOf(trueEdge to outInfo, falseEdge to unreachableInfo)
                                    }
                                    return mapOf(falseEdge to unreachableInfo)
                                }
                                trueEdge?.let{return mapOf(trueEdge to outInfo)}
                                return mapOf()
                            } else throw Exception("guard value is neither 0 nor 1, should not typecheck")
                        }
                        Definition.Top -> { // this means we have not yet processed node (random ordering!!)
//                            throw Exception("variable in if guard is UNDEFINED, should not typecheck!!")
                            return n.edges.associateWith { outInfo }
                        }
                        is Definition.DesignatedMeeter -> throw Exception("pls do not meet")
                    }
                }

                is CFGNode.Gets -> {
                    outInfo.varVals[n.varName] = abstractInterpretation(n.expr, varVals = outInfo.varVals)
                    return n.edges.associateWith { outInfo }
                }

                else -> {
                    return n.edges.associateWith { outInfo }
                } // no change
            }

        } else {
            return n.edges.associateWith { unreachableInfo } // we return T, T vec when unreachable
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

                    else -> leftAbs // consider if this needs to be cloned, supposed to do bot=bot, top=top, ignore meeter
                }
            }

            is CFGExpr.Const -> Definition.Data(expr.value)
            is CFGExpr.Label -> Definition.Bottom // globals, not currently doing this
            is CFGExpr.Mem -> Definition.Bottom // not currently saving the memory
            is CFGExpr.Var -> varVals.getOrDefault(expr.name, Definition.Top)
        }
    }

//    fun absInterpMoosh


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
    class Info(val unreachability: Unreachability, val varVals: MutableMap<String, Definition>) : EdgeValues() {
        override val pretty: String = "($unreachability, ($varVals))"
        fun copy() : Info {
            return Info(unreachability, varVals.toMutableMap())
        }
    }
}