package optimize.dataflow

import optimize.IROptimizer.Graphable
import optimize.cfg.CFG
import optimize.cfg.CFGNode
import optimize.cfg.Edge


sealed class CFGFlow<Lattice : EdgeValues>(val cfg: CFG) : Graphable {
    val values: MutableMap<Edge, Lattice> = mutableMapOf()
    abstract fun meet(e1: Lattice, e2: Lattice): Lattice
    abstract fun transition(n: CFGNode, inInfo: Lattice): Map<Edge, Lattice>
    abstract val top: Lattice
    abstract fun run()
    abstract val name: String

    abstract class Forward<Lattice : EdgeValues>(cfg: CFG) : CFGFlow<Lattice>(cfg) {
        override fun run() {
            var counter = 0
            val nodes = cfg.getNodes()
            val worklist = nodes.toMutableSet()
            nodes.forEach { it.edges.forEach { values[it] = top } }
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
            println("it took $counter to terminate $name")
        }

        private fun bigMeet(predEdges: Set<Edge>?, values: Map<Edge, Lattice>): Lattice {
            var out: Lattice? = null
            predEdges?.forEach {
                val edgeVal = values[it]!! // every edge should have a value
                val _out = out
                out = if (_out == null) edgeVal else meet(_out, edgeVal)
            }
            return out ?: top
        }
    }

    override fun graphViz(): String {
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