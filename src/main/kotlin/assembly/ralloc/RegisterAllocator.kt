package assembly.ralloc

import assembly.x86.*
import typechecker.EtaFunc
import assembly.x86.Instruction.*

sealed class RegisterAllocator(val assembly: x86CompUnit, val functionTypes: Map<String, EtaFunc>) {
    /** In the current calling conventions, the callee-save registers are rbp, rsp, rbx, and r12–r15. */
    val calleeSavedRegs = listOf(
        Register.x86(Register.x86Name.RBX),
        Register.x86(Register.x86Name.R12),
        Register.x86(Register.x86Name.R13),
        Register.x86(Register.x86Name.R14),
        Register.x86(Register.x86Name.R15)
    )

    val callerSavedRegs = listOf(
        Register.x86(Register.x86Name.RAX),
        Register.x86(Register.x86Name.RCX),
        Register.x86(Register.x86Name.RDX),
        Register.x86(Register.x86Name.RDI),
        Register.x86(Register.x86Name.RSI),
        Register.x86(Register.x86Name.R8),
        Register.x86(Register.x86Name.R9),
        Register.x86(Register.x86Name.R10),
        Register.x86(Register.x86Name.R11)
    )

    fun allocate(): x86CompUnit {
        return allocateCompUnit(assembly)
    }

    private fun allocateCompUnit(n: x86CompUnit): x86CompUnit {
        return x86CompUnit(n.name, n.functions.map { allocateFunction(it) }, n.globals)
    }

    abstract fun allocateFunction(n: x86FuncDecl): x86FuncDecl

    fun replaceInsnRegisters(insn: Instruction, replaceMap: Map<String, Int>): Instruction {
        return when (insn) {
            is Arith -> {
                when (insn) {
                    is Arith.ADD -> Arith.ADD(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Arith.LEA -> Arith.LEA(
                        replaceDestRegister(insn.dest, replaceMap),
                        when (val v = replaceSrcRegister(insn.src, replaceMap)) {
                            is Source.MemorySrc -> v
                            else -> throw Exception("reason to refactor")
                        }
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

            is CMP -> CMP(
                replaceDestRegister(insn.dest, replaceMap),
                replaceSrcRegister(insn.src, replaceMap)
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

            is MOV -> MOV(
                replaceDestRegister(insn.dest, replaceMap),
                replaceSrcRegister(insn.src, replaceMap)
            )
            is POP -> POP(replaceRegister(insn.dest, replaceMap))
            is PUSH -> PUSH(replaceRegister(insn.arg, replaceMap))
            is TEST -> TEST(
                replaceRegister(insn.reg1, replaceMap),
                replaceRegister(insn.reg2, replaceMap)
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

            is DIV -> DIV(replaceRegister(insn.divisor, replaceMap))
            is IMULSingle -> IMULSingle(replaceRegister(insn.factor, replaceMap))

            is CALL, is COMMENT, is CQO, is ENTER, is Label, is LEAVE, is NOP, is RET, is Jump, is PAD,
            is CALLERSAVEPUSH, is CALLERSAVEPOP -> insn

            is Arith.DEC -> Arith.DEC(replaceDestRegister(insn.dest, replaceMap))
            is Arith.INC -> Arith.INC(replaceDestRegister(insn.dest, replaceMap))
        }
    }

    private fun replaceDestRegister(d: Destination, replaceMap: Map<String, Int>): Destination {
        return when (d) {
            is Destination.MemoryDest -> Destination.MemoryDest(replaceMemRegister(d.m, replaceMap))
            is Destination.RegisterDest -> Destination.RegisterDest(replaceRegister(d.r, replaceMap))
        }
    }

    private fun replaceSrcRegister(s: Source, replaceMap: Map<String, Int>): Source {
        return when (s) {
            is Source.ConstSrc -> s
            is Source.MemorySrc -> Source.MemorySrc(replaceMemRegister(s.m, replaceMap))
            is Source.RegisterSrc -> Source.RegisterSrc(replaceRegister(s.r, replaceMap))
        }
    }

    private fun replaceMemRegister(m: Memory, replaceMap: Map<String, Int>): Memory {
        return when (m) {
            is Memory.LabelMem -> m
            is Memory.RegisterMem -> Memory.RegisterMem(
                if (m.base == null) null else replaceRegister(m.base, replaceMap),
                if (m.index == null) null else replaceRegister(m.index, replaceMap),
                shift = m.shift, offset = m.offset
            )
        }
    }

    abstract fun replaceRegister(r: Register, replaceMap: Map<String, Int>, size: Int = 64): Register.x86

}