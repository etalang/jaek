package optimize.cfg

import optimize.dataflow.Element.IntersectNodes

class Dominating(cfg: CFG) : CFGFlow.Forward<Dominating.Info>(cfg), UseDef {
    override val values: MutableMap<Edge, Info> = mutableMapOf()

    private final val moosher = IntersectNodes.DesignatedMeeter().meet // small frown :c but necessary

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
        return n.edges.associateWith { outInfo }
    }

    /** the nodes that dominate this edge */
    data class Info(val doms: IntersectNodes) : EdgeAnnos() {
        override val pretty: String = doms.pretty;//.replace("(.{80})", "$1\n");
    }

    override val top: Info = Info(IntersectNodes.Top)
    override val name: String = "Dominating"
}