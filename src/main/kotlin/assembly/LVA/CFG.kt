package assembly.LVA

import optimize.IROptimizer.Graphable

class CFG(val nodes: List<CFGNode>, val name: String, val targets: Map<String, CFGNode>) : Graphable {
    val start = nodes.first()

    override fun graphViz(): String {
        val map = mutableMapOf<CFGNode, String>()
        nodes.forEachIndexed { index, t -> map[t] = "n$index" }
        return buildString {
            appendLine("digraph $name {")
            appendLine("\trankdir=\"TB\"")
            appendLine("\tfontname = \"Helvetica,Arial,sans-serif\";")
            appendLine("\tnode [fontname = \"Helvetica,Arial,sans-serif\";];")
            appendLine("\tedge [fontname = \"Helvetica,Arial,sans-serif\";];")

            map.forEach { appendLine("\t${it.value} [shape=rectangle; label=\"${it.key.insn}\";];") }
            map.forEach { (from, graphKey) ->
                for (to in from.to.map { targets[it] }.filterNotNull()) {
                    appendLine("\t$graphKey -> ${map[to]};")
                }
            }
            appendLine("}")
        }
    }

    fun preds(): Map<CFGNode, Set<CFGNode>> {
        val map = mutableMapOf<CFGNode, MutableSet<CFGNode>>()
        nodes.forEach { node ->
            node.to.map { targets[it] }.filterNotNull().forEach {
                map.computeIfAbsent(it) { mutableSetOf() }.add(node)
            }
        }
        return map
    }
}