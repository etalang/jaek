package optimize.dataflow

import optimize.cfg.CFG
import optimize.cfg.CFGExpr
import optimize.cfg.CFGNode
import optimize.cfg.Edge
import optimize.dataflow.Element.*
import optimize.dataflow.Properties.*

class CondConstProp(cfg : CFG) : CFGFlow.Forward<CondConstProp.Info>(cfg), Def {
    override val top: Info = Info(Unreachability.Bottom, mutableMapOf())
    override val name: String = "Conditional Constant Propogation"

    override fun transition(n: CFGNode, inInfo: Info): Map<Edge, Info> {
        val allVarsTop = inInfo.varVals.mapValues { Definition.Top }
        var outInfo = Info(Unreachability.Bottom, allVarsTop.toMutableMap())
        if(inInfo.unreachability == Unreachability.Bottom) { // we return T, T vec when unreachable
            when (n) {
                is CFGNode.If -> TODO()
                is CFGNode.Gets -> TODO()
                else -> {}
            }

        }
        return n.edges.associateWith { outInfo }
    }

    fun abstractInterpretation(expr: CFGExpr) : Definition {
        TODO()
        // 2+2 =4, 2+ top = top, 2+bot = bot, f(x)= bot
        // fx doesn't even exist in CFGExpr
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
    class Info(val unreachability: Unreachability, val varVals : MutableMap<String, Definition>) : EdgeValues() {
        override val pretty: String = "($unreachability, ($varVals))"
    }
}