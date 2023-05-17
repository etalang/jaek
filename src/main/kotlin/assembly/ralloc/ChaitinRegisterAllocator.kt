package assembly.ralloc

import assembly.ConventionalCaller
import assembly.LVA.LiveVariableAnalysis
import assembly.x86.*
import assembly.x86.Instruction.*
import assembly.x86.Memory.LabelMem
import assembly.x86.Memory.RegisterMem
import assembly.x86.Register.Abstract
import assembly.x86.Register.x86
import assembly.x86.Register.x86Name.*
import typechecker.EtaFunc
import java.io.File

class ChaitinRegisterAllocator(assembly: x86CompUnit, functionTypes: Map<String, EtaFunc>) :
    RegisterAllocator(assembly, functionTypes) {
    val K = 16

    // needs to be exact same ordering as declaration of enum x86Name in Register
    val regOrder = listOf(
        x86(RAX),
        x86(RBX),
        x86(RCX),
        x86(RDX),
        x86(RSP),
        x86(RBP),
        x86(RDI),
        x86(RSI),
        x86(R8),
        x86(R9),
        x86(R10),
        x86(R11),
        x86(R12),
        x86(R13),
        x86(R14),
        x86(R15)
    )

    // tracking offset map of spilled temps per function (func name -> (temp name -> offset))
    val fullOffsetMap: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

    init {
        for (funcDecl in assembly.functions) {
            fullOffsetMap[funcDecl.name] = mutableMapOf()
//            fullGeneratedTempCounts[funcDecl.name] = mutableMapOf()
        }
    }

    var debugLoopCtr = 0
    override fun allocateFunction(n: x86FuncDecl): x86FuncDecl {
        val calleeTemps: Map<x86, Abstract> = calleeSavedRegs.associateWith { calleeSpill() }
        val saveCallees =
            calleeSavedRegs.map { MOV(Destination.RegisterDest(calleeTemps[it]!!), Source.RegisterSrc(it)) }
        val saveCallers: MutableList<Instruction> = callerSavedRegs.map { PUSH(it) }.toMutableList()
        saveCallers.add(0, PAD())
        val popCallers: MutableList<Instruction> = callerSavedRegs.reversed().map { POP(it) }.toMutableList()
        popCallers.add(Arith.ADD(Destination.RegisterDest(x86(RSP)), Source.ConstSrc(8)))
        val withCalleeSaved: MutableList<Instruction> = saveCallees.toMutableList()

        val funcType = functionTypes[n.name]!!
        val cc = ConventionalCaller(funcType)
        val populateArguments: MutableList<Instruction> = mutableListOf()
        for (i in 1..funcType.argCount) populateArguments.add(
            MOV(
                Destination.RegisterDest(Abstract("_ARG$i")), cc.getArg(i)
            )
        )
        withCalleeSaved.addAll(populateArguments)

        for (insn in n.body) {
            if (insn is LEAVE) {
                withCalleeSaved.addAll(calleeSavedRegs.map {
                    MOV(
                        Destination.RegisterDest(it), Source.RegisterSrc(calleeTemps[it]!!)
                    )
                })
            }
            if (insn is CALLERSAVEPUSH) {
                withCalleeSaved.addAll(saveCallers)
                continue
            }
            if (insn is CALLERSAVEPOP) {
                withCalleeSaved.addAll(popCallers)
                continue
            }
            withCalleeSaved.add(insn)
        }
        return chitLoop(x86FuncDecl(n.name, withCalleeSaved))
    }

    fun chitLoop(n: x86FuncDecl): x86FuncDecl {
        debugLoopCtr++
        println("${n.name} allocation round $debugLoopCtr")

        // LIVENESS ANALYSIS
        val dataflow = LiveVariableAnalysis(n)
        File("dataflow${n.name}${debugLoopCtr}.dot").writeText(dataflow.graphViz())

        // BUILD INTERFERENCE GRAPH
        val interferenceGraph = InterferenceGraph(dataflow, n.body.flatMap { it.involved }.toSet())

        val worklistMoves = mutableSetOf<InterferenceGraph.Move>()
        //Procedure Build
        dataflow.cfg.nodes.forEach {
            if (it.insn is MOV && it.insn.dest is Destination.RegisterDest && it.insn.src is Source.RegisterSrc) {
                val move = InterferenceGraph.Move(
                    it.insn.dest.r, it.insn.src.r
                )
                for (reg in (it.insn.def union it.insn.use)) {
                    interferenceGraph.moveList.computeIfAbsent(reg) { mutableSetOf() }.add(move)
                    //TODO: maybe sub from live if we are bold
                }
                worklistMoves.add(move)
            }
            val live = dataflow.liveOut[it]!! union it.insn.def
            for (d in it.insn.def) {
                for (l in live) {
                    interferenceGraph.addEdge(l, d)
                }
            }
        }

        // WORKLISTS DECLARATIONS/INITIALIZATION
        val worklist = Worklist(interferenceGraph, K, n.body, worklistMoves)


        File("ig${debugLoopCtr}.dot").writeText(interferenceGraph.graphViz())

        // LOOP
        while (worklist.simplifyWorkList.isNotEmpty() // || worklist.worklistMoves.isNotEmpty()
            || worklist.freezeWorkList.isNotEmpty() || worklist.spillWorkList.isNotEmpty()) {
            if (worklist.simplifyWorkList.isNotEmpty()) simplify(worklist)
//            else if (worklist.worklistMoves.isNotEmpty()) coalesce(worklist)
            else if (worklist.freezeWorkList.isNotEmpty()) freeze(worklist)
            else if (worklist.spillWorkList.isNotEmpty()) selectSpill(worklist)
        }

        // COLORING
        worklist.assignColors()
        println(interferenceGraph.colors)

        // check for spills -- if there are spills, have to repeat >.<
        return if (worklist.spilledNodes.isNotEmpty()) {
            val spillInsns = rewriteSpills(n, worklist)
            val nextFuncDecl = x86FuncDecl(n.name, spillInsns)
            File("assemblyIteration${debugLoopCtr}.txt").writeText(nextFuncDecl.toString())
            chitLoop(nextFuncDecl)
        } else {
            val replaceMap = mutableMapOf<String, Int>()
            for (reg in worklist.ig.colors.keys) {
                if (reg is Abstract) replaceMap[reg.name] = worklist.ig.colors[reg]!!
            }
            val allocatedInsns = n.body.map { replaceInsnRegisters(it, replaceMap) }
            // maybe want to remove redundant moves in allocatedInsns?
            val postprocessInsns = postprocess(n.name, allocatedInsns, worklist)
            debugLoopCtr = 0
            x86FuncDecl(n.name, postprocessInsns)
        }
    }

    /* MAIN HELPERS APPEARING IN THE FUNCTION */
    private fun simplify(worklist: Worklist) {
        val reg = worklist.simplifyWorkList.elementAt(0)
        worklist.simplifyWorkList.remove(reg)
        worklist.selectStack.add(reg)
        val adjs = worklist.adjacent(reg)
        for (neighbor in adjs) {
//            if (neighbor is Abstract)
            worklist.decrementDegree(neighbor)
        }
    }

    private fun coalesce(worklist: Worklist) {
        val m = worklist.worklistMoves.elementAt(0)
        val x = worklist.getAlias(m.dest)
        val y = worklist.getAlias(m.src)
        val u = (if (y is x86) y else x)
        val v = (if (y is x86) x else y)
        worklist.worklistMoves.remove(m)
        if (u == v) {
            worklist.coalescedMoves.add(m)
            worklist.addWorkList(u)
        } else if (v is x86 && worklist.ig.adjSet.contains(u to v)) {
            worklist.constrainedMoves.add(m)
            worklist.addWorkList(u)
            worklist.addWorkList(v)
        } else if (u is x86 && v is Abstract) { // TODO: CHECK IF THIS IS OK TO ENFORCE
            var isOK = true
            val vNeighbors = worklist.adjacent(v)
            for (t in vNeighbors) {
                isOK = isOK && worklist.OK(t, u)
            }
            if (isOK) {
                worklist.coalescedMoves.add(m)
                worklist.combine(u, v)
                worklist.addWorkList(u)
            }
        } else if (u is Abstract && worklist.conservative(worklist.adjacent(u) union worklist.adjacent(v))) {
            worklist.coalescedMoves.add(m)
            worklist.combine(u, v)
            worklist.addWorkList(u)
        } else worklist.activeMoves.add(m)
    }

    private fun freeze(worklist: Worklist) {
        val u = worklist.freezeWorkList.elementAt(0)
        worklist.freezeWorkList.remove(u)
        worklist.simplifyWorkList.add(u)
        worklist.freezeMoves(u)
    }

    private fun heuristic(regSet: Set<Register>): Register {
        // HEURISTIC: TRY TO PICK A NON-SPILLED NODE
        val nonSpills = mutableSetOf<Register>()
        for (reg in regSet) {
            if (reg is Abstract && !reg.name.startsWith("\$S")) {
                nonSpills.add(reg)
            }
        }
        if (nonSpills.isNotEmpty()) return nonSpills.random()
        return regSet.random()
    }

    private fun selectSpill(worklist: Worklist) {
        val m =
            heuristic(worklist.spillWorkList) // worklist.spillWorkList.random()  // TODO: MUST pick w/ heuristic instead
        worklist.spillWorkList.remove(m)
        worklist.simplifyWorkList.add(m)
        worklist.freezeMoves(m)
    }

    /**
     * replaceMap is a map mapping every abstract register in the function body to the corresponding x86 register.
     * indices map the register at that index in regOrder
     */
    override fun replaceRegister(r: Register, replaceMap: Map<String, Int>, size: Int): x86 {
        return when (r) {
            is Abstract -> {
                if (size == 64) regOrder[replaceMap[r.name]!!]
                else {
                    regOrder[replaceMap[r.name]!!].copy(size = 8)
                }
            }

            is x86 -> r
        }
    }

    /**
     * postProcessInsns filters out all redundant moves created by move coalescing
     *
     * TODO: add support for calling conventions
     */
    private fun postprocess(name: String, insns: List<Instruction>, worklist: Worklist): List<Instruction> {
        val filteredInsns = insns.filter {
            if (it is MOV && it.dest is Destination.RegisterDest && it.src is Source.RegisterSrc) {
                !(it.dest.r is x86 && it.src.r is x86 && it.dest.r.name == it.src.r.name)
            } else true
        }

        val numTemps = fullOffsetMap[name]!!.keys.size
        val padTemps = if (numTemps % 2 == 1) 1 else 0
        val argumentsTemps = mutableListOf<Instruction>(ENTER(8L * (numTemps + padTemps)))

//        val funcType = functionTypes[name]!!
//        val cc = ConventionalCaller(funcType)
//        for (i in 1..funcType.argCount) argumentsTemps.add(
//            MOV(
//                Destination.RegisterDest(regOrder[worklist.ig.colors[Abstract("_ARG$i")]!!]),
//                cc.getArg(i)
//            )
//        )
        argumentsTemps.addAll(filteredInsns)
        return argumentsTemps
    }

    fun rewriteSpills(funcDecl: x86FuncDecl, worklist: Worklist): List<Instruction> {
        // build offset map (which might involve previous state)
        val spilledInvolvementCounters = mutableMapOf<String, Int>() // fullGeneratedTempCounts[funcDecl.name]!!
        val offsetMap = fullOffsetMap[funcDecl.name]!!
        for (temp in worklist.spilledNodes) {
            if (temp is Abstract) {
                spilledInvolvementCounters[temp.name] = 0
                offsetMap[temp.name] = offsetMap.keys.size + 1
            } else { // TODO: bandaid fix?
                throw Exception("cannot spill a machine register to stack!")
            }
        }
        // pass through instructions now
        val spilledInsns = mutableListOf<Instruction>()
        for (insn in funcDecl.body) {
            val renameMap = mutableMapOf<String, String>()
            val spilledInvolved = insn.involved intersect worklist.spilledNodes
            val useInsns = mutableListOf<Instruction>()
            val defInsns = mutableListOf<Instruction>()
            for (r in spilledInvolved) {
                if (r is Abstract) {
                    val regCount = spilledInvolvementCounters[r.name]!!
                    val regName = spill()
                    renameMap[r.name] = regName

                    if (r in insn.use) {
                        useInsns.add(
                            MOV(
                                Destination.RegisterDest(Abstract(regName)),
                                Source.MemorySrc(RegisterMem(base = x86(RBP), offset = -8L * offsetMap[r.name]!!))
                            )
                        )
                    }
                    if (r in insn.def) {
                        defInsns.add(
                            MOV(
                                Destination.MemoryDest(
                                    RegisterMem(
                                        base = x86(RBP), offset = -8L * offsetMap[r.name]!!
                                    )
                                ), Source.RegisterSrc(Abstract(regName))
                            )
                        )
                    }
                    spilledInvolvementCounters[r.name] = regCount + 1
                }
            }
            spilledInsns.addAll(useInsns)
            if (renameMap.keys.isEmpty()) {
                spilledInsns.add(insn)
            } else {
                spilledInsns.add(renameInsnRegisters(insn, renameMap))
            }
            spilledInsns.addAll(defInsns)
        }
        fullOffsetMap[funcDecl.name] = offsetMap // may not be necessary
        return spilledInsns
    }

    private fun renameInsnRegisters(insn: Instruction, renameMap: Map<String, String>): Instruction {
        return when (insn) {
            is Arith -> {
                when (insn) {
                    is Arith.ADD -> Arith.ADD(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )

                    is Arith.LEA -> Arith.LEA(
                        renameDestRegister(insn.dest, renameMap),
                        when (val v = renameSrcRegister(insn.src, renameMap)) {
                            is Source.MemorySrc -> v
                            else -> throw Exception("reason to refactor")
                        }
                    )

                    is Arith.MUL -> Arith.MUL(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )

                    is Arith.SUB -> Arith.SUB(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )
                }
            }

            is CMP -> CMP(
                renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
            )

            is Logic -> {
                when (insn) {
                    is Logic.AND -> Logic.AND(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )

                    is Logic.OR -> Logic.OR(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )

                    is Logic.SHL -> Logic.SHL(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )

                    is Logic.SHR -> Logic.SHR(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )

                    is Logic.XOR -> Logic.XOR(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )

                    is Logic.SAR -> Logic.SAR(
                        renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap)
                    )
                }
            }

            is MOV -> MOV(renameDestRegister(insn.dest, renameMap), renameSrcRegister(insn.src, renameMap))
            is POP -> POP(renameRegister(insn.dest, renameMap))
            is PUSH -> PUSH(renameRegister(insn.arg, renameMap))
            is TEST -> TEST(
                renameRegister(insn.reg1, renameMap), renameRegister(insn.reg2, renameMap)
            )

            is JumpSet -> {
                when (insn) { // need to indicate to renameRegister that we need the 8 bit versions
                    is JumpSet.SETB -> JumpSet.SETB(renameRegister(insn.reg, renameMap))
                    is JumpSet.SETG -> JumpSet.SETG(renameRegister(insn.reg, renameMap))
                    is JumpSet.SETGE -> JumpSet.SETGE(renameRegister(insn.reg, renameMap))
                    is JumpSet.SETL -> JumpSet.SETL(renameRegister(insn.reg, renameMap))
                    is JumpSet.SETLE -> JumpSet.SETLE(renameRegister(insn.reg, renameMap))
                    is JumpSet.SETNZ -> JumpSet.SETNZ(renameRegister(insn.reg, renameMap))
                    is JumpSet.SETZ -> JumpSet.SETZ(renameRegister(insn.reg, renameMap))
                }
            }

            is DIV -> DIV(renameRegister(insn.divisor, renameMap))
            is IMULSingle -> IMULSingle(renameRegister(insn.factor, renameMap))

            is CALL, is COMMENT, is CQO, is ENTER, is Label, is LEAVE, is NOP, is RET, is Jump, is PAD, is CALLERSAVEPOP, is CALLERSAVEPUSH -> insn

            is Arith.DEC -> Arith.DEC(renameDestRegister(insn.dest, renameMap))
            is Arith.INC -> Arith.INC(renameDestRegister(insn.dest, renameMap))
        }
    }

    private fun renameDestRegister(dest: Destination, renameMap: Map<String, String>): Destination {
        return when (dest) {
            is Destination.MemoryDest -> Destination.MemoryDest(renameMemoryRegister(dest.m, renameMap))
            is Destination.RegisterDest -> Destination.RegisterDest(renameRegister(dest.r, renameMap))
        }
    }

    private fun renameSrcRegister(src: Source, renameMap: Map<String, String>): Source {
        return when (src) {
            is Source.ConstSrc -> src
            is Source.MemorySrc -> Source.MemorySrc(renameMemoryRegister(src.m, renameMap))
            is Source.RegisterSrc -> Source.RegisterSrc(renameRegister(src.r, renameMap))
        }
    }

    private fun renameMemoryRegister(m: Memory, renameMap: Map<String, String>): Memory {
        return when (m) {
            is LabelMem -> m
            is RegisterMem -> RegisterMem(
                if (m.base == null) null else renameRegister(m.base, renameMap),
                if (m.index == null) null else renameRegister(m.index, renameMap),
                shift = m.shift,
                offset = m.offset
            )
        }

    }

    private fun renameRegister(r: Register, renameMap: Map<String, String>): Register {
        return when (r) {
            is x86 -> r
            is Abstract -> {
                if (r.name in renameMap.keys) {
                    Abstract(renameMap[r.name]!!, size = r.size)
                } else r
            }
        }
    }

    private var freshCalleeTempCount = 0
    private fun calleeSpill(): Abstract {
        freshCalleeTempCount++
        return Abstract("\$CE$freshCalleeTempCount")
    }

    private var freshCallerTempCount = 0
    private fun callerSpill(): Abstract {
        freshCallerTempCount++
        return Abstract("\$CR$freshCallerTempCount")
    }

    private var freshSpillTempCount = 0
    private fun spill(): String {
        freshSpillTempCount++
        return ("\$S$freshSpillTempCount")
    }

}