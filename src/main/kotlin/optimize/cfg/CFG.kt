package optimize.cfg

import optimize.IROptimizer.Graphable

class CFG(val start: CFGNode.Start, val function: String) : Graphable {
    /** compute sparingly <3 */
    fun getNodes(): Set<CFGNode> {
        val list: MutableSet<CFGNode> = mutableSetOf()
        val visited: MutableSet<CFGNode> = mutableSetOf()
        val stack: ArrayDeque<CFGNode> = ArrayDeque()
        stack.addFirst(start)
        while (stack.isNotEmpty()) {
            val node = stack.removeFirst()
            if (!visited.contains(node)) {
                visited.add(node)
                list.add(node)
                stack.addAll(node.edges.map { it.node })
            }
        }
        return list

    }

    fun getEdges(): Set<Edge> {
        val list: MutableSet<Edge> = mutableSetOf()
        getNodes().forEach { list.addAll(it.edges) }
        return list
    }

    //very loose invariant that not in map <=> no predecessors but really shouldn't rely on that
    fun getPredecessors(): Map<CFGNode, Set<CFGNode>> {
        val map = mutableMapOf<CFGNode, MutableSet<CFGNode>>()
        getNodes().forEach { node ->
            node.edges.forEach {
                map.computeIfAbsent(it.node) { mutableSetOf() }.add(node)
            }
        }
        return map
    }

    fun getPredEdges(): Map<CFGNode, Set<Edge>> {
        val map: MutableMap<CFGNode, MutableSet<Edge>> = mutableMapOf()
        getNodes().forEach { node ->
            node.edges.forEach {
                map.computeIfAbsent(it.node) { mutableSetOf() }.add(it)
            }
        }
        return map
    }

    override fun graphViz(): String {
        val map = mutableMapOf<CFGNode, String>()
        getNodes().forEach { t -> map[t] = "n${t.index}" }
        return buildString {
            appendLine("digraph $function {")
            appendLine("\trankdir=\"TB\"")
            appendLine("\tfontname = \"Helvetica,Arial,sans-serif\";")
            appendLine("\tnode [fontname = \"Helvetica,Arial,sans-serif\";];")
            appendLine("\tedge [fontname = \"Helvetica,Arial,sans-serif\";];")

            map.forEach { appendLine("\t${it.value} [shape=rectangle; label=\"${it.key.pretty}\";];") }
            map.forEach { (from, graphKey) ->
                for (edge in from.edges) {
                    appendLine("\t$graphKey -> ${map[edge.node]} ${if (edge.jump) "[label=\"jump\"]" else ""};")
                }
            }
            appendLine("}")
        }
    }
}