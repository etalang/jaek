package optimize.cfg


sealed class CFGFlow<lattice : EdgeAnnos>(val cfg: CFG) {
    abstract val values: MutableMap<Edge, lattice>
    abstract fun meet(e1: lattice, e2: lattice): lattice
    abstract fun transition(n: CFGNode, inInfo: lattice): Map<Edge, lattice>
    abstract val top: lattice
    abstract fun run()
    abstract val name : String

    abstract class Forward<lattice : EdgeAnnos>(cfg: CFG) : CFGFlow<lattice>(cfg) {
        override fun run() {
            var counter = 0
            val worklist = cfg.getNodes().toMutableSet()
            cfg.getNodes().forEach { it.edges.forEach { values[it] = top } }
            val predEdges = cfg.getPredEdges()
            while (worklist.isNotEmpty() && counter < 10000) { //TODO: let it go later
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
                counter++
            }
            println("it took $counter to terminate :D")
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

    fun graphViz(): String {
//        val map = mutableMapOf<CFGNode, String>()
//        cfg.getNodes().forEach { t -> map[t] = "n${t.index}" }
        val nodes = cfg.getNodes()
        return buildString {
            appendLine("digraph ${cfg.function}_${name} {")
            appendLine("\trankdir=\"TB\"")
            appendLine("\tfontname = \"Helvetica,Arial,sans-serif\";")
            appendLine("\tnode [fontname = \"Helvetica,Arial,sans-serif\";];")
            appendLine("\tedge [fontname = \"Helvetica,Arial,sans-serif\";];")

            nodes.forEach { appendLine("\tn${it.index} [shape=rectangle; label=\" ${it.pretty}\"; xlabel=\"${it.index}\";];") }
            nodes.forEach { from ->
                for (edge in from.edges) {
                    appendLine("\tn${from.index} -> n${edge.node.index} [label=\"    ${values[edge]?.pretty}\"]")
                }
            }
            appendLine("}")
        }
    }

}