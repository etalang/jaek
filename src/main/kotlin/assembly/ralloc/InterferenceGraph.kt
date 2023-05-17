package assembly.ralloc

import assembly.LVA.LiveVariableAnalysis
import assembly.x86.Register
import assembly.x86.Register.x86
import optimize.IROptimizer
import java.util.*

/** InterferenceGraph represents the interferences of all registers? (abstract registers) with other registers */
class InterferenceGraph(val dataflow: LiveVariableAnalysis, involved : Set<Register.Abstract>) : IROptimizer.Graphable {
    val adjList: MutableMap<Register, MutableSet<Register>> = mutableMapOf()
    val adjSet: MutableSet<Pair<Register, Register>> = mutableSetOf()
    val degrees: MutableMap<Register, Int> = mutableMapOf()

    class Move(val dest: Register, val src: Register)

    val moveList: MutableMap<Register, MutableSet<Move>> = mutableMapOf()
    val alias: MutableMap<Register, Register> = mutableMapOf()

    val colors: MutableMap<Register, Int> = mutableMapOf()


    init {
        for (temp in involved) {
            adjList[temp] = mutableSetOf()
            degrees[temp] = 0
        }
    }

    fun addEdge(u: Register, v: Register) {
        if (Pair(u, v) !in adjSet && u != v) {
            adjSet.add(u to v)
            adjSet.add(v to u)
            if (u !is x86) {
                adjList[u]!!.add(v)
                degrees[u]!!.plus(1)
            }
            if (v !is x86) {
                adjList[v]!!.add(u)
                degrees[v]!!.plus(1)
            }
        }
    }


    override fun graphViz(): String {
        return buildString {
            appendLine("graph INTERFERE {")
            appendLine("\trankdir=\"TB\"")
            appendLine("\tfontname = \"Helvetica,Arial,sans-serif\";")
            appendLine("\tnode [fontname = \"Helvetica,Arial,sans-serif\";];")
            appendLine("\tedge [fontname = \"Helvetica,Arial,sans-serif\";];")

            adjList.keys.forEach {
                appendLine("\t${
                    it.toString().filter { it != '$' }
                } [shape=rectangle; label=\"${it}, ${colors[it]}\";];")
            }

            Register.x86Name.values().forEach {
                appendLine("\t${
                    it.toString().lowercase()
                } [shape=rectangle; label=\"$it\";];")
            }

            adjList.forEach { startReg, regSet ->
                regSet.forEach {
                    appendLine("\t${startReg.toString().filter { it != '$' }} -- ${
                        it.toString().filter { it != '$' }
                    };")
                }
            }
            appendLine("}")
        }
    }
}
//            if (adjList.keys.contains(u) && !adjList[u]!!.contains(v)) {
//                if (u !is x86) {
//                    adjList[u]?.add(v)
//                    degrees[u] = degrees[u]?.plus(1) ?: -1
//                }
//                if (v !is x86) {
//                    adjList[v]?.add(u)
//                    degrees[v] = degrees[v]?.plus(1) ?: -1
//                }
//            } else if (!adjList.keys.contains(u)) {
//                if (u !is x86) {
//                    adjList[u] = mutableSetOf(v)
//                    degrees[u] = 1
//                }
//                if (v !is x86) {
//                    adjList[v] = mutableSetOf(u)
//                    degrees[v] = 1
//                }
//            }


//    fun constructGraph() {
//        // TODO: the following block is to prevent null lookups
//        val encountered = insns.flatMap { it.involved }.toSet()
//        for (temp in encountered) {
//            adjList[temp] = mutableSetOf()
//            degrees[temp] = 0
//        }
//
//
//        for (p in precolored) {
//            adjList[p] = mutableSetOf()
//            degrees[p] = Int.MAX_VALUE
//            colors[p] = p.name.ordinal
//        }
//
//        for (p1 in precolored) {
//            for (p2 in precolored) {
//                addEdge(p1, p2)
//                addEdge(p2, p1)
//            }
//        }
//
//        for (conflictSet in liveIns.values) {
//            for (u in conflictSet) { // more elegant way to iterate over all pairs?
//                for (v in conflictSet) {
////                        if (u is Abstract)
//                    addEdge(u, v)
////                        if (v is Abstract)
//                    addEdge(v, u)
//
//                }
//            }
//        }
//
//        for (node in liveIns.keys) {
//            val outEdges = node.to.mapNotNull { cfg.targets[it] }
//            val liveOut = mutableSetOf<Register>()
//            for (cfgn in outEdges) {
//                liveOut.addAll(liveIns[cfgn]!!)
//            }
//            for (r1 in node.insn.def) {
//                for (r2 in liveOut) {
//                    addEdge(r1, r2)
//                    addEdge(r2, r1)
//                }
//            }
//        }
//
//        for (insn in insns) {
//            if (insn is Instruction.MOV
//                && insn.dest is Destination.RegisterDest && insn.src is Source.RegisterSrc
//                && !(insn.dest.r is x86 && insn.src.r is x86)
//            ) {
//                for (reg in insn.involved) {
//                    if (moveList.keys.contains(reg)) {
//                        moveList[reg]?.add(Move(insn.dest.r, insn.src.r))
//                    }
//                }
//            }
//        }
//

//        // APPEL IMPLEMENTATION
//        var live = setOf<Register>()
//        for (insn in insns.reversed()) {
//            if (insn is Instruction.MOV && insn.dest is Destination.RegisterDest
//                && insn.src is Source.RegisterSrc) {
//                live = live.minus(insn.use)
//                for (n in insn.def union insn.use) {
//                    if (moveList.keys.contains(n)) {
//                        moveList[n]?.add(Move(insn.dest.r, insn.src.r))
//                    }
//                    else {
//                        moveList[n] = mutableSetOf(Move(insn.dest.r, insn.src.r))
//                    }
//                }
//            }
//            live = live.union(insn.def)
//            for (d in insn.def) {
//                for (l in live) {
//                    addEdge(l, d)
//                }
//            }
//            live = live.minus(insn.def).union(insn.use)
//        }


//    }

