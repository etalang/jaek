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

    override val defaultTile: Tile.Regular
        get() {
            val builder = TileBuilder.Regular(1, this)
            val cc = ConventionalCaller(args.size, n_returns.toInt())

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
            val didWePad: Boolean
            val returnsThatRequiresUsToFuckWithRSP = (n_returns.toInt() - 2).coerceAtLeast(0)
            // we have pushed one more arg if we have large returns
            val pushedArgs = (args.size - 6).coerceAtLeast(0) + (if (n_returns.toInt() > 2) 1 else 0)
            val shitStacked = (returnsThatRequiresUsToFuckWithRSP + pushedArgs)
            if (shitStacked % 2 > 0) {
                didWePad = true
                builder.add(COMMENT("THIS IS FOR PADDING"))
                builder.add(Arith.SUB(RegisterDest(Register.x86(Register.x86Name.RSP)), ConstSrc(8L)))
            } else {
                didWePad = false
            }

            for (i in args.size - 1 downTo 0) {
                val argNum = i + 1
                val tile = args[i].optimalTile()
                builder.consume(tile)
                builder.add(cc.putArg(argNum, tile.outputRegister))
            }

            builder.add(CALL(Label(target.l, false)))

            if (pushedArgs > 0 || didWePad) {
                builder.add(
                    listOf(
                        COMMENT("THIS REMOVES THE PADDING AND DESTROYS THE STACK"), Arith.ADD(
                            RegisterDest(Register.x86(Register.x86Name.RSP)),
                            ConstSrc(8L * (pushedArgs + (if (didWePad) 1 else 0)))
                        )
                    )
                )
            }

            for (i in 1 .. n_returns.toInt()) {
                builder.add(cc.getReturn(i))
            }

            return builder.build()
        }

    override fun findBestTile() {}
}