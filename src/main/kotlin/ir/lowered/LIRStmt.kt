package ir.lowered

//import assembly.Tile
import assembly.tile.BuiltTile
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRCJump as JIRCJump
import edu.cornell.cs.cs4120.etac.ir.IRCallStmt as JIRCallStmt
import edu.cornell.cs.cs4120.etac.ir.IRJump as JIRJump
import edu.cornell.cs.cs4120.etac.ir.IRLabel as JIRLabel
import edu.cornell.cs.cs4120.etac.ir.IRMove as JIRMove
import edu.cornell.cs.cs4120.etac.ir.IRReturn as JIRReturn
import edu.cornell.cs.cs4120.etac.ir.IRStmt as JIRStmt

/** IRStmt represents a statement **/
sealed class LIRStmt : LIRNode.TileableNode<BuiltTile.RegularTile>() {
    abstract override val java: JIRStmt;

    sealed class FlatStmt : LIRStmt()
    sealed class EndBlock : FlatStmt()

    /** IRMove represents moving the result of an expression to a destination**/
    class LIRMove(val dest: LIRExpr, val expr: LIRExpr) : FlatStmt() {
        override val java: JIRMove = factory.IRMove(dest.java, expr.java)

        override val defaultTile: BuiltTile.RegularTile
            get() = expr.optimalTile().let { srcTile ->
                when (dest) {
                    is LIRExpr.LIRMem -> {
                        if (dest.address is LIRExpr.LIRName) { //TODO: maybe don't need this?
                            BuiltTile.RegularTile(
                                srcTile.instructions.plus(
                                    Instruction.MOV(
                                        Destination.MemoryDest(Memory.LabelMem(Label(dest.address.l, false))),
                                        Source.RegisterSrc(srcTile.outputRegister)
                                    ),
                                ), 1
                            )
                        } else {
                            val destTile = dest.optimalTile()
                            BuiltTile.RegularTile(
                                srcTile.instructions.plus(destTile.instructions).plus(
                                    Instruction.MOV(
                                        Destination.MemoryDest(Memory.RegisterMem(destTile.outputRegister, null)),
                                        Source.RegisterSrc(srcTile.outputRegister)
                                    )
                                ), 1
                            )
                        }
                    }

                    is LIRExpr.LIRTemp -> {
                        BuiltTile.RegularTile(
                            srcTile.instructions.plus(
                                Instruction.MOV(
                                    Destination.RegisterDest(Register.Abstract(dest.name)),
                                    Source.RegisterSrc(srcTile.outputRegister)
                                )
                            ), 1
                        )
                    }

                    else -> throw Exception("charles :(")
                }
            }

        override fun findBestTile() {
            attempt(coolTiling())
        }

        private fun coolTiling(): BuiltTile.RegularTile? {
            return null
        }

    }

    /** IRJump represents a jump to address [address]
     *
     **/
    class LIRJump(val address: LIRExpr.LIRName) : EndBlock() {
        override val java: JIRJump = factory.IRJump(address.java)

        override val defaultTile
            get() =
                BuiltTile.RegularTile(listOf(Instruction.Jump.JMP(Location(Label(address.l, false)))), 1)

        override fun findBestTile() {}
    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class LIRCJump(val guard: LIRExpr, val trueBranch: LIRLabel, val falseBranch: LIRLabel?) :
        EndBlock() {
        override val java: JIRCJump =
        //WE SHOULDN'T EVER CALL THIS : UNSUPPORTED OPERATION
            //(WHEN WE CALL THIS falseBranch SHOULD BE NULL!!!!!!) THUS IT SHOULD BE LIRTrueJump
            if (falseBranch != null) factory.IRCJump(guard.java, trueBranch.l, falseBranch.l)
            else factory.IRCJump(guard.java, trueBranch.l)

        override val defaultTile get() = throw Exception("can't tile non canonical")

        override fun findBestTile() {}

    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class LIRTrueJump(val guard: LIRExpr, val trueBranch: LIRLabel) : EndBlock() {
        override val java: JIRCJump = factory.IRCJump(guard.java, trueBranch.l)

        override val defaultTile
            get() = guard.optimalTile().let {
                BuiltTile.RegularTile(
                    it.instructions.plus(
                        listOf(
                            Instruction.TEST(it.outputRegister, it.outputRegister),
                            Instruction.Jump.JNZ(Location(Label(trueBranch.l, false)))
                        )
                    ), 2
                )
            }


        override fun findBestTile() {}
    }

    /** IRLabel represents giving a name [l] to the next statement **/
    class LIRLabel(val l: String) : FlatStmt() {
        override val java: JIRLabel = factory.IRLabel(l)

        override val defaultTile get() = BuiltTile.RegularTile(listOf(Label(l, TODO())),0)

        override fun findBestTile() {}
    }

    /** IRReturn represents returning 0 or more values in [valList] from the current function **/
    class LIRReturn(val valList: List<LIRExpr>) : EndBlock() {
        override val java: JIRReturn = factory.IRReturn(valList.map { it.java })

        override val defaultTile: BuiltTile.RegularTile
            get() {
                val insns = mutableListOf<Instruction>()
//            val reglst = mutableListOf<Register>()
                if (valList.isNotEmpty()) { // single return
                    val firstReturnTile = valList.first().optimalTile()
                    insns.addAll(firstReturnTile.instructions)
                    insns.add(
                        Instruction.MOV(
                            Destination.RegisterDest(Register.x86(Register.x86Name.RAX)),
                            Source.RegisterSrc(firstReturnTile.outputRegister)
                        )
                    )
                }
                if (valList.size >= 2) { // multireturn
                    val secondReturnTile = valList.get(1).optimalTile()
                    insns.addAll(secondReturnTile.instructions)
                    insns.add(
                        Instruction.MOV(
                            Destination.RegisterDest(Register.x86(Register.x86Name.RDX)),
                            Source.RegisterSrc(secondReturnTile.outputRegister)
                        )
                    )
                }
                if (valList.size > 2) { // begin da push
                    for (i in valList.size - 1 downTo 3) {
                        val returnTile = valList[i].optimalTile()
                        insns.addAll(returnTile.instructions)
                        insns.add(
                            Instruction.MOV(
                                Destination.MemoryDest(
                                    Memory.RegisterMem(
                                        Register.x86(Register.x86Name.RDI), null,
                                        offset = 8L * (i - 3L)
                                    )
                                ),
                                Source.RegisterSrc(returnTile.outputRegister)
                            )
                        )
                    }
                }
                // TODO: test whether this works/ensure that the invariants are preserved so that this works
                insns.add(Instruction.LEAVE())
                insns.add(Instruction.RET())

                return BuiltTile.RegularTile(insns, 1)
            }

        override fun findBestTile() {}
    }

    class LIRCallStmt(val target: LIRExpr.LIRName, val n_returns: Long, val args: List<LIRExpr>) :
        FlatStmt() {
        override val java: JIRCallStmt = factory.IRCallStmt(target.java, n_returns, args.map { it.java })

        override val defaultTile : BuiltTile.RegularTile
            get() {
            val insns = mutableListOf<Instruction>()
            val reglst = mutableListOf<Register>()
            val argNumber = args.size
            var argOffset = 0
            for (arg in args) {
                // ensures arguments are still evaluated left to right
                val argTile = arg.optimalTile()
                reglst.add(argTile.outputRegister)
                insns.addAll(argTile.instructions)
            }
            if (n_returns >= 3) {
                argOffset = 1
                insns.add(
                    Instruction.Arith.SUB(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RSP)),
                        Source.ConstSrc(8L * (n_returns - 2L))
                    ))
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RDI)),
                        Source.RegisterSrc(Register.x86(Register.x86Name.RSP))
                    )
                )
            }
            if (argNumber > 6 - argOffset) {
                for (i in args.size - 1 downTo 6 - argOffset) {
                    insns.add(Instruction.PUSH(reglst[i]))
                }
            }
            if (argNumber > 5 - argOffset) {
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.R9)),
                        Source.RegisterSrc(reglst[5 - argOffset])
                    )
                )
            }
            if (argNumber > 4 - argOffset) {
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.R8)),
                        Source.RegisterSrc(reglst[4 - argOffset])
                    )
                )
            }
            if (argNumber > 3 - argOffset) {
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RCX)),
                        Source.RegisterSrc(reglst[3 - argOffset])
                    )
                )
            }
            if (argNumber > 2 - argOffset) {
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RDX)),
                        Source.RegisterSrc(reglst[2 - argOffset])
                    )
                )
            }
            if (argNumber > 1 - argOffset) {
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RSI)),
                        Source.RegisterSrc(reglst[1 - argOffset])
                    )
                )
            }
            if (n_returns < 3) {
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RDI)),
                        Source.RegisterSrc(reglst.first())
                    )
                )
            }
//            insns.add(
//                Instruction.Logic.AND(
//                    Destination.RegisterDest(Register.x86(Register.x86Name.RSP)),
//                    Source.ConstSrc(-16)
//                ))
            insns.add(Instruction.CALL(Label(target.l, false)))
            if (argNumber > 6 - argOffset) {
                insns.add(
                    Instruction.Arith.ADD(
                        Destination.RegisterDest(Register.x86(Register.x86Name.RSP)),
                        Source.ConstSrc(8L * (argNumber - (6 - argOffset)))
                    ))
            }
            insns.add(
                Instruction.MOV(
                    Destination.RegisterDest(Register.Abstract("_RV1")),
                    Source.RegisterSrc(Register.x86(Register.x86Name.RAX))
                )
            )
            if (n_returns >= 2) {
                insns.add(
                    Instruction.MOV(
                        Destination.RegisterDest(Register.Abstract("_RV2")),
                        Source.RegisterSrc(Register.x86(Register.x86Name.RDX))
                    )
                )
            }
            if (n_returns >= 3) {
                for (i in 3 .. n_returns.toInt()) {
                    insns.add(Instruction.POP(Register.Abstract("_RV$i")))
                }
            }
            return BuiltTile.RegularTile(insns, args.size)
        }

        override fun findBestTile() {}
    }

}
