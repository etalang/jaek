package optimize.dataflow

import optimize.cfg.CFG
import optimize.cfg.CFGNode
import optimize.cfg.Edge
import optimize.dataflow.Element.IntersectNodes

class Dominating(cfg: CFG) : CFGFlow.Forward<Dominating.Info>(cfg) {
    override val top: Info = Info(IntersectNodes.Top)
    override val name: String = "Dominating"

    private val moosher = IntersectNodes.DesignatedMeeter().meet // small frown :c but necessary

    /** in[n] = ∩ out[n'] ∀ (n' predecessors) */
    override fun meet(e1: Info, e2: Info): Info {
        return Info(moosher.meet(e1.doms, e2.doms))
    }

    /** F_N (in) = {n} ∪ in */
    override fun transition(n: CFGNode, inInfo: Info): Map<Edge, Info> {
        val out = when (n) {
            is CFGNode.Start -> IntersectNodes.Data(setOf(n)) // start is only dominated by itself
            else -> {
                when (val nset = inInfo.doms) {
                    IntersectNodes.Bottom -> {
                        println("surprising bottom incoming")
                        IntersectNodes.Data(setOf(n))
                    }

                    is IntersectNodes.Data -> IntersectNodes.Data(setOf(n) union nset.t)
                    IntersectNodes.Top -> IntersectNodes.Top // dominated by everything
                    is IntersectNodes.DesignatedMeeter -> throw Exception("you've committed a sin!!!")
                }
            }
        }
        val outInfo = Info(out)
        return cfg.mm.successorEdges(n).associateWith { outInfo }
    }

    /** the nodes that dominate this edge */
    data class Info(val doms: IntersectNodes) : EdgeValues() {
        override val pretty: String = doms.toString()//.replace("(.{80})", "$1\n");
    }
}