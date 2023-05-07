package optimize.cfg

import optimize.dataflow.Element.IntersectNodes

class Dominating(val cfg: CFG) : CFGFlow.Forward<Dominating.Info>(), UseDef {
    override val values: MutableMap<Edge, Info> = mutableMapOf()

    private final val moosher = IntersectNodes.Top.meet // small frown :c but necessary

    override fun meet(e1: Info, e2: Info): Info {
        TODO()
//        e1.domMap.forEach { t, u ->
//            val otherSet = e2.domMap[t]!! //e2's domain MUST be the set of nodes in the graph, same as e2
//
//        }
//        return Info(moosher.meet(e1.nodes, e2.nodes))
    }

    override fun transition(n: CFGNode, inInfo: Info): Unit {
//        val info = when (n) {
//            is CFGNode.Start -> Info(IntersectNodes.Data(setOf()))
//            else -> Info(when (val nodes = inInfo.nodes) {
//                IntersectNodes.Bottom -> TODO()
//                is IntersectNodes.Data -> TODO()
//                IntersectNodes.Top -> TODO()
//            }
//            )
//        }
//        n.edges.forEach { values[it] = info }
        TODO()
    }

    data class Info(val domMap: MutableMap<CFGNode, IntersectNodes>)
}