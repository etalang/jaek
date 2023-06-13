package assembly.ralloc

import assembly.LVA.LiveVariableAnalysis
import assembly.x86.*
import typechecker.EtaFunc
import java.util.PriorityQueue

class LinearScan(assembly: x86CompUnit, functionTypes: Map<String, EtaFunc>) :
    RegisterAllocator(assembly, functionTypes) {

    override fun allocateFunction(n: x86FuncDecl): x86FuncDecl {
        println("here")
        val lva = LiveVariableAnalysis(n)
        val insns = n.body.filter { it !is Instruction.COMMENT }
        val liveRanges = mutableMapOf<Register.Abstract, Pair<Int, Int>>()
        insns.forEachIndexed { index, instruction ->
            println(instruction)
            val alive = lva.live[instruction]!! union instruction.def
            alive.forEach { reg ->
                if (reg is Register.Abstract) {
//                    println("$reg $index")
                    if (liveRanges.contains(reg)) {
                        val x = liveRanges[reg]!!
                        liveRanges[reg] = Pair(x.first, index)
                    } else {
                        liveRanges[reg] = Pair(index, index)
                    }
                }
            }
        }
        val sorted = liveRanges.map { Box(it.key,it.value.first,it.value.second) }.sortedBy { it.start }
        println(sorted)
        println(liveRanges)

        val R = 11
        val openRegisters = (Register.x86.callerSaved() union setOf(Register.x86(Register.x86Name.RBX),
        Register.x86(Register.x86Name.R15))).toMutableSet()

        val compareByLastInsn: Comparator<Box> = compareBy { it.end }
        val active = PriorityQueue(compareByLastInsn)
        val spilled = mutableSetOf<Box>()
        val assigned = mutableMapOf<Box, Register.x86>()
        sorted.forEach {
            val curr = it.start
            // expire old intervals
             while (active.peek() != null && active.peek().end < curr) {
                 val freed = active.poll()
                 assert(assigned[freed] != null)
                 openRegisters.add(assigned[freed]!!)
            }
            if (active.size >= R) { // heuristic to pick last
                val lastActive = active.last()
                if (lastActive.end > it.end) {
                    if (assigned[lastActive] != null) {
                        val lastAssigned = assigned[lastActive]!!
                        assigned[it] = lastAssigned
                        assigned.remove(lastActive)
                    }
                    spilled.add(lastActive)
                    active.remove(lastActive)
                }
                else {
                    spilled.add(it)
                }
            }
            else {
                val r = selectOpenRegister(insns, it, openRegisters)
                assigned[it] = r
                active.add(it)
            }
        }
        val replaceMap = mutableMapOf<String, Int>()
            assigned.forEach{ (box, reg) -> replaceMap[box.reg.name] = reg.name.ordinal }

        val spillMap = mutableMapOf<String, Int>()
        if (spilled.isNotEmpty()) {
            spilled.forEachIndexed { idx, box -> spillMap[box.reg.name] = idx + 1 }
        }
        val spilledInsns = insns.flatMap { trivialSpill(it, spillMap) }
        val allocatedInsns = spilledInsns.map { replaceInsnRegisters(it, replaceMap)  }
        return x86FuncDecl(n.name, allocatedInsns)
    }

    private fun selectOpenRegister(insns : List<Instruction>, b : Box, rSet : MutableSet<Register.x86>) : Register.x86 {
//        if (insns.size >= b.start && insns[b.start] is Instruction.MOV) { // move coalescing
//
//        }
        for (r in rSet) { // otherwise, try to pick a caller save, if available
            if (r in Register.x86.callerSaved()) {
                rSet.remove(r)
                return r
            }
        }
        val default = rSet.elementAt(0)
        rSet.remove(default)
        return default
    }

    override fun replaceRegister(r: Register, replaceMap: Map<String, Int>, size: Int): Register.x86 {
        return when (r) {
            is Register.Abstract -> {
                if (replaceMap.keys.contains(r.name)) {
                    Register.x86(r.idxTox86Name(replaceMap[r.name]!!))
                }
                else {
                    throw Exception("should be unreachable")
                }
            }
            is Register.x86 -> r
        }
    }

    data class Box(val reg : Register.Abstract, val start: Int, val end : Int)

}