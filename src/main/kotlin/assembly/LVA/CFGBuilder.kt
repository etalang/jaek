package assembly.LVA

import assembly.x86.Instruction
import assembly.x86.Instruction.*
import assembly.x86.Label
import assembly.x86.x86FuncDecl
import java.util.*

class CFGBuilder(val funcDecl: x86FuncDecl) {
    private val nodes: MutableList<CFGNode> = mutableListOf()
    private val targets: MutableMap<String, CFGNode> = mutableMapOf()
    private var pointingTo: String = pointToNext()

    init {

        val instructions: MutableList<Instruction> = LinkedList(funcDecl.body.filter { it !is COMMENT })
        while (instructions.isNotEmpty()) {
            var currInsn = instructions.first()

            //handle labels
            val labels = mutableListOf(pointingTo)
            while (currInsn is Label) {
                labels.add(currInsn.name)
                instructions.removeFirst()
                currInsn = instructions.first()
            }

            currInsn = instructions.removeFirst()
            val next = when (currInsn) {
                is Arith, is CMP, is CQO, is Arith.DEC, is CALLERSAVEPUSH, is CALLERSAVEPOP, is Arith.INC, is CALL,
                is DIV, is ENTER, is IMULSingle, is JumpSet, is LEAVE, is Logic, is MOV, is NOP, is PAD, is POP, is PUSH, is TEST -> {
                    CFGNode(currInsn, listOf(pointToNext()))
                }

                is COMMENT -> throw Exception("charles bypassed the filter!")
                is Label -> throw Exception("charles snuck even more labels in")
                is RET -> CFGNode(currInsn, listOf())
                is Jump.JMP -> CFGNode(currInsn, listOf(currInsn.loc.l.name))
                is Jump -> CFGNode(currInsn, listOf(pointToNext(), currInsn.loc.l.name))
            }

            nodes.add(next)
            labels.forEach {
                val target = targets[it]
                if (target == null) targets[it] = next
            }
        }
    }

    fun build(): CFG {
        return CFG(nodes, funcDecl.name, targets)
    }


    private var freshLabelCount = 0
    private fun pointToNext(): String {
        freshLabelCount++
        pointingTo = "\$X$freshLabelCount"
        return pointingTo
    }
}