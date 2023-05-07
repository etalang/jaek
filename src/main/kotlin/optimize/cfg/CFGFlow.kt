package optimize.cfg


sealed class CFGFlow<lattice>(val cfg: CFG) {
    abstract val values: MutableMap<Edge, lattice>
    abstract fun meet(e1: lattice, e2: lattice): lattice
    abstract fun transition(n: CFGNode, inInfo: lattice): Map<Edge, lattice>
    abstract val top: lattice
    abstract fun run()

    abstract class Forward<lattice>(cfg: CFG) : CFGFlow<lattice>(cfg) {
        override fun run() {
            val worklist = cfg.getNodes().toMutableSet()
            cfg.getNodes().forEach { it.edges.forEach { values[it] = top } }
            val predEdges = cfg.getPredEdges()
            while (worklist.isNotEmpty()) {
                println(worklist)
                val node = worklist.random()
                worklist.remove(node)
                val inInfo = bigMeet(predEdges[node], values)
                val newEdges = transition(node, inInfo)
                node.edges.forEach {
                    val before = values[it]
                    if (before != newEdges[it]) {
                        values[it] = newEdges[it]!!
                        worklist.add(it.node)
                    }
                }
            }
        }

        private fun bigMeet(predEdges: Set<Edge>?, values: Map<Edge, lattice>): lattice {
            var out: lattice? = null
            predEdges?.forEach {
                val edgeVal = values[it]!! // every edge should have a value
                val _out = out
                out = if (_out == null) edgeVal else meet(_out, edgeVal)
            }
            return out ?: top
        }
    }

}