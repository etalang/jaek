package assembly

import assembly.x86.*
import assembly.x86.Destination.MemoryDest
import assembly.x86.Destination.RegisterDest
import assembly.x86.Instruction.*
import assembly.x86.Memory.LabelMem
import assembly.x86.Memory.RegisterMem
import assembly.x86.Register.*
import assembly.x86.Source.*

class RegisterAllocator {
    /** default three registers used in trivial register allocation
     * ASSUME: we don't use these registers ANYWHERE in a nontrivial capacity before we allocate */
    private val defaults = listOf(x86(x86Name.R11), x86(x86Name.R12), x86(x86Name.R13))

    /** map for all temps encountered */
    private val offsetMap = mutableMapOf<String, Int>()

    /* for each instruction:
           * detect the temps/registers in the instruction
           * -- if there are any new abstract ones, add them to our list of offsets we need to keep track of
           * figure out what the abstract registers written to / used were
           * assign all abstract registers to a real one (rax, rcx, rdx here)
           * if there are any read from, read them into their appropriately assigned registers with the correct offsets
           * put in the instruction with all abstract registers replaced
           * if there are any written to, write them into memory
           * */
    fun allocateRegisters(insns: List<Instruction>): MutableList<Instruction> {
        val returnedInsns = mutableListOf<Instruction>()
        for (insn in insns) {
            if (insn !is COMMENT) returnedInsns.add(COMMENT("[AA] $insn"))
            val (written, used) = detectRegisters(insn)
            val mentioned = written union used
            // holds whether each abstract register mentioned should be assigned 0, 1, or 2
            val replaced = mutableMapOf<String, Int>()
            val abstractWritten = mutableSetOf<Abstract>()
            val abstractUsed = mutableSetOf<Abstract>()
            for (r in mentioned) {
                if (r is Abstract) {
                    replaced[r.name] = replaced.keys.size
                    if (r.name !in offsetMap.keys) {
                        offsetMap[r.name] = offsetMap.keys.size + 1
                    }
                    if (r in written) {
                        abstractWritten.add(r)
                    }
                    if (r in used) {
                        abstractUsed.add(r)
                    }
                }
            }

            for (ru in abstractUsed) {
                replaced[ru.name]?.let { idx ->
                    offsetMap[ru.name]?.let { shift ->
                        returnedInsns.add(
                            MOV(
                                RegisterDest(defaults[idx]),
                                MemorySrc(RegisterMem(x86(x86Name.RBP), null, offset = -8L * shift))
                            )
                        )
                    }
                }
            }
            returnedInsns.add(replaceInsnRegisters(insn, replaced))
            for (rw in abstractWritten) {
                replaced[rw.name]?.let { idx ->
                    offsetMap[rw.name]?.let { shift ->
                        returnedInsns.add(
                            MOV(
                                MemoryDest(RegisterMem(x86(x86Name.RBP), null, offset = -8L * shift)),
                                RegisterSrc(defaults[idx])
                            )
                        )
                    }
                }
            }
        }
        returnedInsns.add(0, ENTER(8L * offsetMap.keys.size))
        return returnedInsns
    }

    private fun replaceInsnRegisters(insn: Instruction, replaceMap: Map<String, Int>): Instruction {
        return when (insn) {
            is Arith -> {
                when (insn) {
                    is Arith.ADD -> Arith.ADD(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Arith.DIV -> Arith.DIV(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Arith.LEA -> Arith.LEA(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Arith.MUL -> Arith.MUL(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Arith.SUB -> Arith.SUB(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )
                }
            }

            is CMP -> insn.copy(
                reg1 = replaceRegister(insn.reg1, replaceMap),
                reg2 = replaceRegister(insn.reg2, replaceMap)
            )

            is Logic -> {
                when (insn) {
                    is Logic.AND -> Logic.AND(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Logic.OR -> Logic.OR(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Logic.SHL -> Logic.SHL(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Logic.SHR -> Logic.SHR(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Logic.XOR -> Logic.XOR(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Logic.SAR -> Logic.SAR(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )
                }
            }

            is MOV -> MOV(replaceDestRegister(insn.dest, replaceMap), replaceSrcRegister(insn.src, replaceMap))
            is POP -> insn.copy(dest = replaceRegister(insn.dest, replaceMap))
            is PUSH -> insn.copy(arg = replaceRegister(insn.arg, replaceMap))
            is TEST -> insn.copy(
                reg1 = replaceRegister(insn.reg1, replaceMap),
                reg2 = replaceRegister(insn.reg2, replaceMap)
            )
            is JumpSet -> {
                when (insn) { // need to indicate to replaceRegister that we need the 8 bit versions
                    is JumpSet.SETB -> JumpSet.SETB(replaceRegister(insn.reg, replaceMap, 8))
                    is JumpSet.SETG -> JumpSet.SETG(replaceRegister(insn.reg, replaceMap, 8))
                    is JumpSet.SETGE -> JumpSet.SETGE(replaceRegister(insn.reg, replaceMap, 8))
                    is JumpSet.SETL -> JumpSet.SETL(replaceRegister(insn.reg, replaceMap, 8))
                    is JumpSet.SETLE -> JumpSet.SETLE(replaceRegister(insn.reg, replaceMap, 8))
                    is JumpSet.SETNZ -> JumpSet.SETNZ(replaceRegister(insn.reg, replaceMap, 8))
                    is JumpSet.SETZ -> JumpSet.SETZ(replaceRegister(insn.reg, replaceMap, 8))
                }
            }

            else -> insn
        }
    }

    private fun replaceDestRegister(d: Destination, replaceMap: Map<String, Int>): Destination {
        return when (d) {
            is MemoryDest -> MemoryDest(replaceMemRegister(d.m, replaceMap))
            is RegisterDest -> RegisterDest(replaceRegister(d.r, replaceMap))
        }
    }

    private fun replaceSrcRegister(s: Source, replaceMap: Map<String, Int>): Source {
        return when (s) {
            is ConstSrc -> s
            is MemorySrc -> MemorySrc(replaceMemRegister(s.m, replaceMap))
            is RegisterSrc -> RegisterSrc(replaceRegister(s.r, replaceMap))
        }
    }

    private fun replaceMemRegister(m: Memory, replaceMap: Map<String, Int>): Memory {
        return when (m) {
            is LabelMem -> m
            is RegisterMem -> RegisterMem(
                replaceRegister(m.base, replaceMap),
                if (m.index == null) null else replaceRegister(m.index, replaceMap),
                shift = m.shift, offset = m.offset
            )
        }
    }

    private fun replaceRegister(r: Register, replaceMap: Map<String, Int>, size : Int = 64): x86 {
        return when (r) {
            is Abstract ->
                if (size == 64)
                    defaults[replaceMap[r.name]!!]
                else {
                    defaults[replaceMap[r.name]!!].copy(size = 8)
                }
            is x86 -> r
        }
    }

    /** detectRegisters(insn) returns a pair of sets of registers, the first being the
     * registers written to and the second being the registers read from */
    private fun detectRegisters(insn: Instruction): Pair<Set<Register>, Set<Register>> {
        return when (insn) {
            is Arith -> detectDestRegsWritten(insn.dest) to (detectDestRegsUsed(insn.dest) union detectSrcRegs(insn.src))
            is CMP -> emptySet<Register>() to setOf(insn.reg1, insn.reg2)
            is Logic -> detectDestRegsWritten(insn.dest) to (detectDestRegsUsed(insn.dest) union detectSrcRegs(insn.src))
            is MOV -> detectDestRegsWritten(insn.dest) to (detectDestRegsUsed(insn.dest) union detectSrcRegs(insn.src))
            is POP -> setOf(insn.dest) to emptySet()
            is PUSH -> emptySet<Register>() to setOf(insn.arg)
            is TEST -> emptySet<Register>() to setOf(insn.reg1, insn.reg2)
            is JumpSet ->  emptySet<Register>() to setOf(insn.reg) // TODO: the register being written to is ALSO THE 64 BIT ONE
            else -> emptySet<Register>() to emptySet()
        }
    }

    /** detects the registers written to by the destination */
    private fun detectDestRegsUsed(dest: Destination): Set<Register> {
        return when (dest) {
            is MemoryDest -> detectMemoryRegisters(dest.m)
            is RegisterDest -> setOf(dest.r)
        }
    }

    /** detects the registers used to by the instruction */
    private fun detectDestRegsWritten(dest: Destination): Set<Register> {
        return when (dest) {
            is MemoryDest -> emptySet()
            is RegisterDest -> setOf(dest.r)
        }
    }

    /** detects the registers used in the source */
    private fun detectSrcRegs(src: Source): Set<Register> {
        return when (src) {
            is ConstSrc -> emptySet()
            is MemorySrc -> detectMemoryRegisters(src.m)
            is RegisterSrc -> setOf(src.r)
        }
    }

    private fun detectMemoryRegisters(mem: Memory): Set<Register> {
        return when (mem) {
            is LabelMem -> emptySet()
            is RegisterMem -> {
                val returnedSet = mutableSetOf(mem.base)
                if (mem.index != null)
                    returnedSet.add(mem.index)
                returnedSet
            }
        }
    }
}