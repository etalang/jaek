package assembly.ralloc

import assembly.x86.*
import typechecker.EtaFunc

sealed class RegisterAllocator(val assembly: x86CompUnit, val functionTypes: Map<String, EtaFunc>) {
    /** In the current calling conventions, the callee-save registers are rbp, rsp, rbx, and r12–r15. */
    val calleeSavedRegs = listOf(
        Register.x86(Register.x86Name.RBX),
        Register.x86(Register.x86Name.R12),
        Register.x86(Register.x86Name.R13),
        Register.x86(Register.x86Name.R14),
        Register.x86(Register.x86Name.R15)
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
            is Instruction.Arith -> {
                when (insn) {
                    is Instruction.Arith.ADD -> Instruction.Arith.ADD(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Instruction.Arith.LEA -> Instruction.Arith.LEA(
                        replaceDestRegister(insn.dest, replaceMap),
                        when (val v = replaceSrcRegister(insn.src, replaceMap)) {
                            is Source.MemorySrc -> v
                            else -> throw Exception("reason to refactor")
                        }
                    )

                    is Instruction.Arith.MUL -> Instruction.Arith.MUL(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Instruction.Arith.SUB -> Instruction.Arith.SUB(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )
                }
            }

            is Instruction.CMP -> Instruction.CMP(
                replaceDestRegister(insn.dest, replaceMap),
                replaceSrcRegister(insn.src, replaceMap)
            )

            is Instruction.Logic -> {
                when (insn) {
                    is Instruction.Logic.AND -> Instruction.Logic.AND(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Instruction.Logic.OR -> Instruction.Logic.OR(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Instruction.Logic.SHL -> Instruction.Logic.SHL(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Instruction.Logic.SHR -> Instruction.Logic.SHR(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Instruction.Logic.XOR -> Instruction.Logic.XOR(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )

                    is Instruction.Logic.SAR -> Instruction.Logic.SAR(
                        replaceDestRegister(insn.dest, replaceMap),
                        replaceSrcRegister(insn.src, replaceMap)
                    )
                }
            }

            is Instruction.MOV -> Instruction.MOV(
                replaceDestRegister(insn.dest, replaceMap),
                replaceSrcRegister(insn.src, replaceMap)
            )
            is Instruction.POP -> Instruction.POP(replaceRegister(insn.dest, replaceMap))
            is Instruction.PUSH -> Instruction.PUSH(replaceRegister(insn.arg, replaceMap))
            is Instruction.TEST -> Instruction.TEST(
                replaceRegister(insn.reg1, replaceMap),
                replaceRegister(insn.reg2, replaceMap)
            )

            is Instruction.JumpSet -> {
                when (insn) { // need to indicate to replaceRegister that we need the 8 bit versions
                    is Instruction.JumpSet.SETB -> Instruction.JumpSet.SETB(replaceRegister(insn.reg, replaceMap, 8))
                    is Instruction.JumpSet.SETG -> Instruction.JumpSet.SETG(replaceRegister(insn.reg, replaceMap, 8))
                    is Instruction.JumpSet.SETGE -> Instruction.JumpSet.SETGE(replaceRegister(insn.reg, replaceMap, 8))
                    is Instruction.JumpSet.SETL -> Instruction.JumpSet.SETL(replaceRegister(insn.reg, replaceMap, 8))
                    is Instruction.JumpSet.SETLE -> Instruction.JumpSet.SETLE(replaceRegister(insn.reg, replaceMap, 8))
                    is Instruction.JumpSet.SETNZ -> Instruction.JumpSet.SETNZ(replaceRegister(insn.reg, replaceMap, 8))
                    is Instruction.JumpSet.SETZ -> Instruction.JumpSet.SETZ(replaceRegister(insn.reg, replaceMap, 8))
                }
            }

            is Instruction.DIV -> Instruction.DIV(replaceRegister(insn.divisor, replaceMap))
            is Instruction.IMULSingle -> Instruction.IMULSingle(replaceRegister(insn.factor, replaceMap))

            is Instruction.CALL, is Instruction.COMMENT, is Instruction.CQO, is Instruction.ENTER, is Label, is Instruction.LEAVE, is Instruction.NOP, is Instruction.RET, is Instruction.Jump, is Instruction.PAD -> insn

            is Instruction.Arith.DEC -> Instruction.Arith.DEC(replaceDestRegister(insn.dest, replaceMap))
            is Instruction.Arith.INC -> Instruction.Arith.INC(replaceDestRegister(insn.dest, replaceMap))
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