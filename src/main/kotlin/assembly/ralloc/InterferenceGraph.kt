package assembly.ralloc

import assembly.LVA.CFGNode
import assembly.LVA.CFG
import assembly.x86.Destination
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.Register.*
import assembly.x86.Source
import assembly.x86.Register.x86Name.*

/** InterferenceGraph represents the interferences of all registers? (abstract registers) with other
 * registers */
class InterferenceGraph(val liveIns : Map<CFGNode, Set<Register>>, val cfg : CFG,  val insns : List<Instruction>) {
    val adjList: MutableMap<Register, MutableSet<Register>> = mutableMapOf()
    val degrees: MutableMap<Register, Int> = mutableMapOf()
    val precolored : Set<x86> = setOf(x86(RAX), x86(RBX), x86(RCX), x86(RDX), x86(RDI), x86(RSI), x86(RSP), x86(RBP),
        x86(R8), x86(R9), x86(R10), x86(R11), x86(R12), x86(R13), x86(R14), x86(R15))
    class Move(val dest: Register, val src : Register)
    val moveList: MutableMap<Register, MutableSet<Move>> = mutableMapOf()
    val alias: MutableMap<Register, Register> = mutableMapOf()

    val colors: MutableMap<Register, Int> = mutableMapOf()


    fun addEdge(src: Register, dest: Register) {
        if (src != dest) {
            if (adjList.keys.contains(src) && !adjList[src]!!.contains(dest)) {
                if (src !is x86) {
                    adjList[src]?.add(dest)
                    degrees[src] = degrees[src]?.plus(1) ?: -1
                }
                if (dest !is x86) {
                    adjList[dest]?.add(src)
                    degrees[dest] = degrees[dest]?.plus(1) ?: -1
                }
            }
            else if (!adjList.keys.contains(src)) {
                if (src !is x86) {
                    adjList[src] = mutableSetOf(dest)
                    degrees[src] = 1
                }
                if (dest !is x86) {
                    adjList[dest] = mutableSetOf(src)
                    degrees[dest] = 1
                }
            }
        }
    }


    fun constructGraph() {
        // TODO: the following block is to prevent null lookups
        val encountered = insns.flatMap { it.involved }.toSet()
        for (temp in encountered) {
            adjList[temp] = mutableSetOf()
            degrees[temp] = 0
        }
        for (p in precolored) {
            adjList[p] = mutableSetOf()
            degrees[p] = Int.MAX_VALUE
            colors[p] = p.name.ordinal
        }

        for (p1 in precolored) {
            for (p2 in precolored) {
                addEdge(p1, p2)
                addEdge(p2, p1)
            }
        }

        for (conflictSet in liveIns.values) {
            for (u in conflictSet) { // more elegant way to iterate over all pairs?
                for (v in conflictSet) {
//                        if (u is Abstract)
                    addEdge(u, v)
//                        if (v is Abstract)
                    addEdge(v, u)

                }
            }
        }

        for (node in liveIns.keys) {
            val outEdges = node.to.mapNotNull { cfg.targets[it] }
            val liveOut = mutableSetOf<Register>()
            for (cfgn in outEdges) {
                liveOut.addAll(liveIns[cfgn]!!)
            }
            for (r1 in node.insn.def) {
                for (r2 in liveOut) {
                    addEdge(r1, r2)
                    addEdge(r2, r1)
                }
            }
        }

        for (insn in insns) {
            if (insn is Instruction.MOV
                && insn.dest is Destination.RegisterDest && insn.src is Source.RegisterSrc
                && !(insn.dest.r is x86 && insn.src.r is x86)) {
                for (reg in insn.involved) {
                    if (moveList.keys.contains(reg)) {
                        moveList[reg]?.add(Move(insn.dest.r, insn.src.r))
                    }
                }
            }
        }


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


    }

    
}