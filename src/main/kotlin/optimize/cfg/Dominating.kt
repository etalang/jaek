package optimize.cfg

import optimize.dataflow.Element.IntersectNodes

class Dominating(cfg: CFG) : CFGFlow.Forward<Dominating.Info>(cfg), UseDef {
    override val values: MutableMap<Edge, Info> = mutableMapOf()

    private final val moosher = IntersectNodes.Top.meet // small frown :c but necessary

    /** in[n] = ∩ out[n'] ∀ (n' predecessors) */
    override fun meet(e1: Info, e2: Info): Info {
        val returnMap = mutableMapOf<CFGNode, IntersectNodes>()
        e1.domMap.forEach { t, u ->
            val otherSet = e2.domMap[t]!! //e2's domain MUST be the set of nodes in the graph, same as e2
            returnMap[t] = moosher.meet(u, otherSet)
        }
        return Info(returnMap)
    }

    /** F_N (in) = {n} ∪ in */
    override fun transition(n: CFGNode, inInfo: Info) : Map<Edge, Info> {
        val outInfo = inInfo.copy()
        when (n) {
            is CFGNode.Start -> outInfo.domMap[n] = IntersectNodes.Data(setOf(n)) // start only dominates itself
            else -> {
                when (val nset = outInfo.domMap[n]) {
                    IntersectNodes.Bottom -> {
                        println("THIS SHOULDNT HAPPEN BRUH")
                        outInfo.domMap[n] = IntersectNodes.Data(setOf(n))
                    }

                    is IntersectNodes.Data -> outInfo.domMap[n] = IntersectNodes.Data(setOf(n) union nset.t)
                    IntersectNodes.Top -> {} // dominated by everything, don't need to add self
                    null -> throw Exception("charles is finding new nodes in the graph")
                }
            }
        }
        return n.edges.associateWith{outInfo}
    }

    /** [domMap] domain = cfg.nodes */
    data class Info(val domMap: MutableMap<CFGNode, IntersectNodes>)

    override val top: Info = Info(cfg.getNodes().associateWith { IntersectNodes.Top }.toMutableMap())
}