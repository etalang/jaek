package optimize.cfg

class CFG(val start: CFGNode.Start, val function : String, val nodes : List<CFGNode>) {
//    val nodes : Set<CFGNode> get() = start.reachable
    //TODO: compute nodes on the fly

    fun graphViz(): String {
        val map = mutableMapOf<CFGNode, String>()
        nodes.forEachIndexed { index, t -> map[t] = "n$index" }
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