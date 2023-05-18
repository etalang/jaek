package assembly.LVA

import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.x86FuncDecl
import optimize.IROptimizer.Graphable

class LiveVariableAnalysis(val funcDecl: x86FuncDecl) : Graphable {
    val liveIn: Map<CFGNode, Set<Register>>
    val liveOut: Map<CFGNode, Set<Register>>
    val live: Map<Instruction, Set<Register>>
    val cfg = CFGBuilder(funcDecl).build()

    init {
        val inVals: MutableMap<CFGNode, Set<Register>> = mutableMapOf()
        val outVals: MutableMap<CFGNode, Set<Register>> = mutableMapOf()
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
            outVals[node] = out
            val oldIn = inVals[node]
            if (node.insn is Instruction.PUSH) {
//                println("push on charles")

            }
            val newIn = node.insn.use union (out - node.insn.def)
            if (newIn != oldIn) {
                inVals[node] = newIn
                preds[node]?.forEach { worklist.add(it) }
            }
        }

        liveIn = inVals
        liveIn.forEach {
            if (!it.value.contains(Register.x86(Register.x86Name.RSP))) {
                println()
            }
        }
        liveOut = outVals
        live = liveIn.mapKeys { it.key.insn }
    }

    override fun graphViz(): String {
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