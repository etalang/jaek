package assembly.ralloc

import assembly.LVA.LiveVariableAnalysis
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.x86CompUnit
import assembly.x86.x86FuncDecl
import typechecker.EtaFunc

class LinearScan(assembly: x86CompUnit, functionTypes: Map<String, EtaFunc>) :
    RegisterAllocator(assembly, functionTypes) {
    override fun allocateFunction(n: x86FuncDecl): x86FuncDecl {
        println("here")
        val lva = LiveVariableAnalysis(n)
        val insns = n.body.filter { it !is Instruction.COMMENT }
        val involved = insns.flatMap { it.involved }
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

        val R = 10
        var active = mutableSetOf<Register.Abstract>()
        sorted.forEach {
        }

        println(sorted)
        println(liveRanges)
        return n
    }

    override fun replaceRegister(r: Register, replaceMap: Map<String, Int>, size: Int): Register.x86 {
        TODO("Not yet implemented")
    }

    data class Box(val reg : Register.Abstract, val start: Int, val end : Int, var assigned : Register.x86)
}