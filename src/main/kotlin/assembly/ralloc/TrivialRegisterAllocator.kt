package assembly.ralloc

import assembly.ConventionalCaller
import assembly.x86.*
import assembly.x86.Destination.MemoryDest
import assembly.x86.Destination.RegisterDest
import assembly.x86.Instruction.*
import assembly.x86.Memory.LabelMem
import assembly.x86.Memory.RegisterMem
import assembly.x86.Register.Abstract
import assembly.x86.Register.x86
import assembly.x86.Register.x86Name.*
import assembly.x86.Source.*
import typechecker.EtaFunc

class TrivialRegisterAllocator(assembly: x86CompUnit, functionTypes: Map<String, EtaFunc>) : RegisterAllocator(assembly, functionTypes) {
    /**
     * default three registers used in trivial register allocation ASSUME: we don't use these registers ANYWHERE in a
     * nontrivial capacity before we allocate
     */
    private val defaults = listOf(x86(R12), x86(R13), x86(R14))

//    fun allocate(): x86CompUnit {
//        return allocateCompUnit(assembly)
//    }

//    private fun allocateCompUnit(n: x86CompUnit): x86CompUnit {
//        return x86CompUnit(n.name, n.functions.map { allocateFunction(it) }, n.globals)
//    }

    override fun allocateFunction(n: x86FuncDecl): x86FuncDecl {
        return x86FuncDecl(n.name, allocateRegisters(n.body, n.name))
    }

    /* for each instruction:
           * detect the temps/registers in the instruction
           * -- if there are any new abstract ones, add them to our list of offsets we need to keep track of
           * figure out what the abstract registers written to / used were
           * assign all abstract registers to a real one (rax, rcx, rdx here)
           * if there are any read from, read them into their appropriately assigned registers with the correct offsets
           * put in the instruction with all abstract registers replaced
           * if there are any written to, write them into memory
           * */
    private fun allocateRegisters(insns: List<Instruction>, name: String): List<Instruction> {
        val offsetMap: Map<String, Int> = populateMap(insns)
        val numTemps = offsetMap.keys.size
        val padTemps = if (numTemps % 2 == 1) 1 else 0
        val returnedInsns = mutableListOf<Instruction>(
            ENTER(8L * (numTemps + padTemps))
        )

        //callee saved regs
        returnedInsns.addAll(calleeSavedRegs.map { PUSH(it) })

        val funcType = functionTypes[name]!!
        val cc = ConventionalCaller(funcType)
        val populateArguments: MutableList<Instruction> = mutableListOf()
        for (i in 1..funcType.argCount) populateArguments.add(
            MOV(
                RegisterDest(Abstract("_ARG$i")),
                cc.getArg(i)
            )
        )
        val allocatedInsns = populateArguments + insns

        for (insn in allocatedInsns) {
            if (insn !is COMMENT) returnedInsns.add(COMMENT("[AA] $insn"))
            /** holds whether each abstract register mentioned should be assigned 0, 1, or 2 * */
            val replaced = mutableMapOf<String, Int>()
            val encountered = insn.involved.toList()
            assert(encountered.size <= 3)
            encountered.forEachIndexed { index, register -> replaced[register.name] = index }
            if (insn is CALLERSAVEPUSH) {
                callerSavedRegs.forEach { returnedInsns.add(PUSH(it)) }
                continue
            }
            if (insn is CALLERSAVEPOP) {
                callerSavedRegs.reversed().forEach{ returnedInsns.add(POP(it)) }
                continue
            }
            if (insn is LEAVE) { //saved regs pop back off in reverse order
                returnedInsns.addAll(calleeSavedRegs.reversed().map { POP(it) })
            }

            for (ru in encountered) {
                replaced[ru.name]?.let { idx ->
                    offsetMap[ru.name]?.let { shift ->
                        returnedInsns.add(
                            MOV(
                                RegisterDest(defaults[idx]),
                                MemorySrc(RegisterMem(x86(RBP), null, offset = -8L * shift))
                            )
                        )
                    }
                }
            }
            returnedInsns.add(replaceInsnRegisters(insn, replaced))
            for (rw in encountered) {
                replaced[rw.name]?.let { idx ->
                    offsetMap[rw.name]?.let { shift ->
                        returnedInsns.add(
                            MOV(
                                MemoryDest(RegisterMem(x86(RBP), null, offset = -8L * shift)),
                                RegisterSrc(defaults[idx])
                            )
                        )
                    }
                }
            }

        }
        return returnedInsns
    }

    private fun populateMap(insns: List<Instruction>): Map<String, Int> {
        val encountered = insns.flatMap { it.involved }.map { it.name }.toSet().toList()
        val map = mutableMapOf<String, Int>()
        encountered.forEachIndexed { index, t ->
            map[t] = index + 1
        }
        return map
    }

//    private fun replaceInsnRegisters(insn: Instruction, replaceMap: Map<String, Int>): Instruction {
//        return when (insn) {
//            is Arith -> {
//                when (insn) {
//                    is Arith.ADD -> Arith.ADD(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//
//                    is Arith.LEA -> Arith.LEA(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        when (val v = replaceSrcRegister(insn.src, replaceMap)) {
//                            is MemorySrc -> v
//                            else -> throw Exception("reason to refactor")
//                        }
//                    )
//
//                    is Arith.MUL -> Arith.MUL(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//
//                    is Arith.SUB -> Arith.SUB(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//                }
//            }
//
//            is CMP -> CMP(
//                replaceDestRegister(insn.dest, replaceMap),
//                replaceSrcRegister(insn.src, replaceMap)
//            )
//
//            is Logic -> {
//                when (insn) {
//                    is Logic.AND -> Logic.AND(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//
//                    is Logic.OR -> Logic.OR(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//
//                    is Logic.SHL -> Logic.SHL(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//
//                    is Logic.SHR -> Logic.SHR(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//
//                    is Logic.XOR -> Logic.XOR(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//
//                    is Logic.SAR -> Logic.SAR(
//                        replaceDestRegister(insn.dest, replaceMap),
//                        replaceSrcRegister(insn.src, replaceMap)
//                    )
//                }
//            }
//
//            is MOV -> MOV(replaceDestRegister(insn.dest, replaceMap), replaceSrcRegister(insn.src, replaceMap))
//            is POP -> POP(replaceRegister(insn.dest, replaceMap))
//            is PUSH -> PUSH(replaceRegister(insn.arg, replaceMap))
//            is TEST -> TEST(
//                replaceRegister(insn.reg1, replaceMap),
//                replaceRegister(insn.reg2, replaceMap)
//            )
//
//            is JumpSet -> {
//                when (insn) { // need to indicate to replaceRegister that we need the 8 bit versions
//                    is JumpSet.SETB -> JumpSet.SETB(replaceRegister(insn.reg, replaceMap, 8))
//                    is JumpSet.SETG -> JumpSet.SETG(replaceRegister(insn.reg, replaceMap, 8))
//                    is JumpSet.SETGE -> JumpSet.SETGE(replaceRegister(insn.reg, replaceMap, 8))
//                    is JumpSet.SETL -> JumpSet.SETL(replaceRegister(insn.reg, replaceMap, 8))
//                    is JumpSet.SETLE -> JumpSet.SETLE(replaceRegister(insn.reg, replaceMap, 8))
//                    is JumpSet.SETNZ -> JumpSet.SETNZ(replaceRegister(insn.reg, replaceMap, 8))
//                    is JumpSet.SETZ -> JumpSet.SETZ(replaceRegister(insn.reg, replaceMap, 8))
//                }
//            }
//
//            is DIV -> DIV(replaceRegister(insn.divisor, replaceMap))
//            is IMULSingle -> IMULSingle(replaceRegister(insn.factor, replaceMap))
//
//            is CALL, is COMMENT, is CQO, is ENTER, is Label, is LEAVE, is NOP, is RET, is Jump, is PAD -> insn
//
//            is Arith.DEC -> Arith.DEC(replaceDestRegister(insn.dest, replaceMap))
//            is Arith.INC -> Arith.INC(replaceDestRegister(insn.dest, replaceMap))
//        }
//    }
//
//    private fun replaceDestRegister(d: Destination, replaceMap: Map<String, Int>): Destination {
//        return when (d) {
//            is MemoryDest -> MemoryDest(replaceMemRegister(d.m, replaceMap))
//            is RegisterDest -> RegisterDest(replaceRegister(d.r, replaceMap))
//        }
//    }
//
//    private fun replaceSrcRegister(s: Source, replaceMap: Map<String, Int>): Source {
//        return when (s) {
//            is ConstSrc -> s
//            is MemorySrc -> MemorySrc(replaceMemRegister(s.m, replaceMap))
//            is RegisterSrc -> RegisterSrc(replaceRegister(s.r, replaceMap))
//        }
//    }
//
//    private fun replaceMemRegister(m: Memory, replaceMap: Map<String, Int>): Memory {
//        return when (m) {
//            is LabelMem -> m
//            is RegisterMem -> RegisterMem(
//                if (m.base == null) null else replaceRegister(m.base, replaceMap),
//                if (m.index == null) null else replaceRegister(m.index, replaceMap),
//                shift = m.shift, offset = m.offset
//            )
//        }
//    }

    /** replaceMap is a map mapping every abstract register in an instruction to the corresponding index of the
     * register in the instruction, which is treated as an index into the list of default allocated registers. */
    override fun replaceRegister(r: Register, replaceMap: Map<String, Int>, size: Int): x86 {
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
}