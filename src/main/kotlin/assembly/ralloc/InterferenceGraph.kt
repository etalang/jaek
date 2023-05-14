package assembly.ralloc

import assembly.LVA.CFGNode
import assembly.x86.Destination
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.Register.*
import assembly.x86.Source
import assembly.x86.Register.x86Name.*

/** InterferenceGraph represents the interferences of all registers? (abstract registers) with other
 * registers */
class InterferenceGraph(val liveIns : Map<CFGNode, Set<Register>>, val insns : List<Instruction>) {
    val adjList: MutableMap<Register, MutableSet<Register>> = mutableMapOf()
    val degrees: MutableMap<Register, Int> = mutableMapOf()
    val precolored : Set<x86> = setOf(x86(RAX), x86(RBX), x86(RCX), x86(RDX), x86(RDI), x86(RSI), x86(RSP), x86(RBP),
        x86(R8), x86(R9), x86(R10), x86(R11), x86(R12), x86(R13), x86(R14), x86(R15))
    class Move(val dest: Register, val src : Register)
    val moveList: MutableMap<Register, MutableSet<Move>> = mutableMapOf()
    val alias: MutableMap<Register, Register> = mutableMapOf()

    val colors: MutableMap<Register, Int> = mutableMapOf()


    fun addEdge(src: Register, dest: Register) {
        if (adjList.keys.contains(src) && src !is x86) {
            adjList[src]?.add(dest)
            degrees[src] = degrees[src]?.plus(1) ?: -1
        }
        else {
            if (src !is x86) {
                adjList[src] = mutableSetOf(dest)
                degrees[src] = 1
            }
        }
    }


    fun constructGraph() {
        val encountered = insns.flatMap { it.involved }.toSet()
        for (temp in encountered) {
            adjList[temp] = mutableSetOf()
            degrees[temp] = 0
        }
        for (p in precolored) {
            adjList[p] = mutableSetOf()
            degrees[p] = 0
            colors[p] = p.name.ordinal
        }
        for (conflictSet in liveIns.values) {
            for (u in conflictSet) { // more elegant way to iterate over all pairs?
                for (v in conflictSet) {
                    if (u != v) {
//                        if (u is Abstract)
                        addEdge(u, v)
//                        if (v is Abstract)
                        addEdge(v, u)
                    }
                }
            }
        }

        for (insn in insns) {
            if (insn is Instruction.MOV && insn.dest is Destination.RegisterDest && insn.src is Source.RegisterSrc) {
                for (reg in insn.involved) {
                    if (moveList.keys.contains(reg)) {
                        moveList[reg]?.add(Move(insn.dest.r, insn.src.r))
                    }
                }
            }
        }
    }

    
}