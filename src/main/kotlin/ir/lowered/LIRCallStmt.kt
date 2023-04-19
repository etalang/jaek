package ir.lowered

import assembly.ConventionalCaller
import assembly.Tile
import assembly.TileBuilder
import assembly.x86.Destination.RegisterDest
import assembly.x86.Instruction.*
import assembly.x86.Label
import assembly.x86.Register
import assembly.x86.Source.ConstSrc
import assembly.x86.Source.RegisterSrc
import edu.cornell.cs.cs4120.etac.ir.IRCallStmt

class LIRCallStmt(val target: LIRExpr.LIRName, val n_returns: Long, val args: List<LIRExpr>) : LIRStmt.FlatStmt() {
    override val java: IRCallStmt = factory.IRCallStmt(target.java, n_returns, args.map { it.java })

    private val callerSavedRegs = listOf(
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

    override val defaultTile: Tile.Regular
        get() {
            val builder = TileBuilder.Regular(1, this)
            val cc = ConventionalCaller(args.size, n_returns.toInt())
            callerSavedRegs.forEach { builder.add(PUSH(it)) }
            //first make the space for extra returns :)
            if (n_returns >= 3) {
                builder.add(
                    Arith.SUB(
                        RegisterDest(Register.x86(Register.x86Name.RSP)), ConstSrc(8L * (n_returns - 2L))
                    )
                )
                builder.add(
                    MOV(
                        RegisterDest(Register.x86(Register.x86Name.RDI)),
                        RegisterSrc(Register.x86(Register.x86Name.RSP))
                    )
                )
            }

            //alignment
//            val didWePad: Boolean
//            val returnsThatRequiresUsToFuckWithRSP = (n_returns.toInt() - 2).coerceAtLeast(0)
//            // we have pushed one more arg if we have large returns
            val pushedArgs = (args.size - 6).coerceAtLeast(0) + (if (n_returns.toInt() > 2) 1 else 0)
//            val shitStacked = (returnsThatRequiresUsToFuckWithRSP + pushedArgs)
//            if (shitStacked % 2 > 0) {
//                didWePad = true
//                builder.add(COMMENT("THIS IS FOR PADDING"))
//                builder.add(Arith.SUB(RegisterDest(Register.x86(Register.x86Name.RSP)), ConstSrc(8L)))
//            } else {
//                didWePad = false
//            }

            for (i in args.size - 1 downTo 0) {
                val argNum = i + 1
                val tile = args[i].optimalTile()
                builder.consume(tile)
                builder.add(cc.putArg(argNum, tile.outputRegister))
            }
            //reg allocator adds padding if needed here
            builder.add(CALL(Label(target.l, false)))
            //reg allocator removes padding if needed
            if (pushedArgs > 0) {
                builder.add(
                    listOf(
                        COMMENT("THIS REMOVES THE EXTRA ARGS FROM THE STACK"), Arith.ADD(
                            RegisterDest(Register.x86(Register.x86Name.RSP)),
                            ConstSrc(8L * pushedArgs)
                        )
                    )
                )
            }

            for (i in 1 .. n_returns.toInt()) {
                builder.add(cc.getReturn(i))
            }

            //after everything is done, restore caller saved
            callerSavedRegs.reversed().forEach { builder.add(POP(it)) }
            return builder.build()
        }

    override fun findBestTile() {}
}