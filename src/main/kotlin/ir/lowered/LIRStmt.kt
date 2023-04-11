package ir.lowered

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
sealed class LIRStmt<TileType> : LIRNode.TileableNode<TileType>() where TileType : BuiltTile{
    override abstract val java: JIRStmt;

    sealed class FlatStmt<TileType> : LIRStmt<TileType>() where TileType : BuiltTile
    sealed class EndBlock<TileType> : FlatStmt<TileType>() where TileType : BuiltTile

    /** IRMove represents moving the result of an expression to a destination**/
    class LIRMove(val dest: LIRExpr, val expr: LIRExpr) : FlatStmt<BuiltTile.RegularTile>() {
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
                            val destTile = expr.optimalTile()
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
                                    Destination.RegisterDest(Register.Abstract((dest).name)),
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
     * IMPORTANT INVARIANT: ANY INSTANCES OF [LIRExpr.LIRName] MUST BE IMMEDIATELY IN [address]
     * **/
    class LIRJump(val address: LIRExpr.LIRName) : EndBlock<BuiltTile.RegularTile>() {
        override val java: JIRJump = factory.IRJump(address.java)

        override val defaultTile
            get() =
                BuiltTile.RegularTile(listOf(Instruction.Jump.JMP(Location(Label(address.l, false)))), 1)

        override fun findBestTile() {}
    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class LIRCJump(val guard: LIRExpr, val trueBranch: LIRLabel, val falseBranch: LIRLabel?) : EndBlock<BuiltTile.RegularTile>() {
        override val java: JIRCJump =
        //WE SHOULDN'T EVER CALL THIS : UNSUPPORTED OPERATION
            //(WHEN WE CALL THIS falseBranch SHOULD BE NULL!!!!!!) THUS IT SHOULD BE LIRTrueJump
            if (falseBranch != null) factory.IRCJump(guard.java, trueBranch.l, falseBranch.l)
            else factory.IRCJump(guard.java, trueBranch.l)

        override val defaultTile get() = throw Exception("can't tile non canonical")

        override fun findBestTile() {}

    }

    /** IRCJump represents a jump to [trueBranch] if [guard] is non-zero and a jump to [falseBranch] otherwise**/
    class LIRTrueJump(val guard: LIRExpr, val trueBranch: LIRLabel) : EndBlock<BuiltTile.RegularTile>() {
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
    class LIRLabel(val l: String) : FlatStmt<BuiltTile.LabelTile>() {
        override val java: JIRLabel = factory.IRLabel(l)

        override val defaultTile get() = BuiltTile.LabelTile(l)

        override fun findBestTile() {}
    }

    /** IRReturn represents returning 0 or more values in [valList] from the current function **/
    class LIRReturn(val valList: List<LIRExpr>) : EndBlock<BuiltTile.RegularTile>() {
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

    class LIRCallStmt(val target: LIRExpr.LIRName, val n_returns: Long, val args: List<LIRExpr>) : FlatStmt<BuiltTile.RegularTile>() {
        override val java: JIRCallStmt = factory.IRCallStmt(target.java, n_returns, args.map { it.java })

        override val defaultTile get() = TODO("Not yet implemented")

        override fun findBestTile() {
            TODO("Not yet implemented")
        }
    }

}
