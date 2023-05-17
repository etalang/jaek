package optimize.dataflow

import optimize.IROptimizer.Graphable
import optimize.cfg.CFG
import optimize.cfg.CFGNode
import optimize.cfg.Edge

const val THRESHOLD: Int = 10000

sealed class CFGFlow<Lattice : EdgeValues>(val cfg: CFG) : Graphable {
    val values: MutableMap<Edge, Lattice> = mutableMapOf()
    abstract fun meet(e1: Lattice, e2: Lattice): Lattice
    abstract fun transition(n: CFGNode, argumentInfo: Lattice): Map<Edge, Lattice>
    abstract val top: Lattice
    abstract fun run()
    abstract val name: String
    private val mm = cfg.mm
    abstract fun runWithWorklist(wklist: Set<CFGNode>)

    abstract class Forward<Lattice : EdgeValues>(cfg: CFG) : CFGFlow<Lattice>(cfg) {
        override fun run() {
            runWithWorklist(cfg.mm.allNodes().toMutableSet())
        }

        override fun runWithWorklist(wklist : Set<CFGNode>) {
            var counter = 0
            val nodes = cfg.mm.allNodes()
            val worklist = wklist.toMutableSet()
            nodes.forEach { n -> cfg.mm.successorEdges(n).forEach { if (!values.contains(it)) values[it] = top } }
            while (worklist.isNotEmpty() && counter < THRESHOLD) {
                val node = worklist.first()
                worklist.remove(node)
                val inInfo = bigMeet(cfg.mm.predecessorEdges(node))
                val newEdges = transition(node, inInfo)
                cfg.mm.successorEdges(node).forEach {
                    val before = values[it]
                    if (before != newEdges[it]) {
                        if (counter > THRESHOLD - 10) {
                            val after = newEdges[it]
                            if (before is CondConstProp.Info && after is CondConstProp.Info) {
                                require(before.unreachability == after.unreachability)
                                require(before.varVals == after.varVals)
                            }
//                            println("${before} IS NOT ${newEdges[it]}")
                        }
                        values[it] = newEdges[it]!!
                        worklist.add(it.node)
                    }
                }
                counter++
            }
//            println("it took $counter to terminate $name")
            if (counter == THRESHOLD) {
                println("TOOK TOO MANY!")
            }
        }
    }

    abstract class Backward<Lattice : EdgeValues>(cfg: CFG) : CFGFlow<Lattice>(cfg) {
        override fun run() {
            runWithWorklist(cfg.mm.allNodes().toMutableSet())
        }

        override fun runWithWorklist(wklist: Set<CFGNode>) {
            var counter = 0
            val worklist = wklist.toMutableSet()
            cfg.mm.allNodes().forEach { n -> cfg.mm.successorEdges(n).forEach { if (!values.contains(it)) values[it] = top } }
            while (worklist.isNotEmpty() && counter < THRESHOLD) { // TODO: let it go later
                val node = worklist.random()
                worklist.remove(node)
                val outInfo = bigMeet(cfg.mm.successorEdges(node))
                val newEdges = transition(node, outInfo)
                cfg.mm.predecessorEdges(node).forEach {
                    val before = values[it]
                    if (before != newEdges[it]) {
                        values[it] = newEdges[it]!!
                        worklist.add(it.from)
                    }
                }
                counter++
            }
//            println("it took $counter to terminate $name")
            if (counter == 10000) {
                println("TOOK TOO MANY!")
            }
        }

    }

    fun bigMeet(predEdges: Set<Edge>?): Lattice {
        var out: Lattice? = null
        predEdges?.forEach {
            val edgeVal = values[it]!! // every edge should have a value
            val outCopy = out
            out = if (outCopy == null) edgeVal else meet(outCopy, edgeVal)

        }
        return out ?: top
    }

    override fun graphViz(): String {
        val nodes = cfg.reachableNodes()
        return buildString {
            appendLine("digraph ${cfg.function.drop(1)}_${name.filterNot { it.isWhitespace() }} {")
            appendLine("\trankdir=\"TB\"")
            appendLine("\tfontname = \"Helvetica,Arial,sans-serif\";")
            appendLine("\tnode [fontname = \"Helvetica,Arial,sans-serif\";];")
            appendLine("\tedge [fontname = \"Helvetica,Arial,sans-serif\";];")

            nodes.forEach { appendLine("\tn${it.index} [shape=rectangle; label=\" ${it.pretty}\"; xlabel=\"${it.index}\";];") }
            nodes.forEach { from ->
                for (edge in cfg.mm.successorEdges(from)) {
                    appendLine("\tn${from.index} -> n${edge.node.index} [label=\"    ${values[edge]?.pretty}\"]")
                }
            }
            appendLine("}")
        }
    }

}

interface PostProc {
    fun postprocess()
}