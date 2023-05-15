package assembly.LVA

import assembly.x86.Register
import assembly.x86.x86FuncDecl

class LiveVariableAnalysis(val funcDecl: x86FuncDecl) {
    val liveIn: Map<CFGNode, Set<Register>>
    private val cfg = CFGBuilder(funcDecl).build()

    init {
        val inVals: MutableMap<CFGNode, Set<Register>> = mutableMapOf()
        cfg.nodes.forEach {
            inVals[it] = emptySet()
        }
        val preds = cfg.preds()
        val worklist = cfg.nodes.toMutableSet()
        while (worklist.isNotEmpty()) {
            val node = worklist.random()
            worklist.remove(node)
            val outEdges = node.to.mapNotNull { cfg.targets[it] }
            var out = emptySet<Register>()
            outEdges.forEach { out = out union (inVals[it] ?: emptySet()) }
            val oldIn = inVals[node]
            val newIn = node.insn.use union (out - node.insn.def)
            if (newIn != oldIn) {
                inVals[node] = newIn
                preds[node]?.forEach { worklist.add(it) }
            }
        }

        liveIn = inVals
    }

    fun graphViz(): String {
        val map = mutableMapOf<CFGNode, String>()
        cfg.nodes.forEachIndexed { index, t -> map[t] = "n$index" }
        return buildString {
            appendLine("digraph ${funcDecl.name}_LVA {")
            appendLine("\trankdir=\"TB\"")
            appendLine("\tfontname = \"Helvetica,Arial,sans-serif\";")
            appendLine("\tnode [fontname = \"Helvetica,Arial,sans-serif\";];")
            appendLine("\tedge [fontname = \"Helvetica,Arial,sans-serif\";];")

            map.forEach { appendLine("\t${it.value} [shape=rectangle; label=\"${it.key.insn}\";];") }
            map.forEach { (from, graphKey) ->
                for (to in from.to.map { cfg.targets[it] }.filterNotNull()) {
                    appendLine("\t$graphKey -> ${map[to]} [label=\"${liveIn[to]}\"];")
                }
            }
            appendLine("}")
        }
    }
}