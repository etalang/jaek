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
            //push caller saved registers pseudo-instruction
            builder.add(CALLERSAVEPUSH())
//            callerSavedRegs.forEach { builder.add(PUSH(it)) } //ODD but + callee = EVEN

            //alignment before arguments get pushed on the stack
            val pushedReturns = (n_returns.toInt() - 2).coerceAtLeast(0)
            // we have pushed one more arg if we have large returns
            val maxArgsInRegister = (if (n_returns.toInt() > 2) 5 else 6)
            val pushedArgs = (args.size - maxArgsInRegister).coerceAtLeast(0)

            val didWePad: Boolean;
            if ((pushedReturns + pushedArgs) % 2 > 0) {
                didWePad = true
                builder.add(COMMENT("adding padding"))
                builder.add(PAD())
            } else {
                didWePad = false
            }

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


            for (i in args.size - 1 downTo 0) {
                val argNum = i + 1
                val tile = args[i].optimalTile()
                builder.consume(tile)
                builder.add(cc.putArg(argNum, tile.outputRegister))
            }

            //THE CALL
            builder.add(CALL(Label(target.l, false)))

            //remove arguments from stack
            if (pushedArgs > 0) {
                builder.add(COMMENT("THIS REMOVES THE EXTRA ARGS FROM THE STACK"))
                builder.add(Arith.ADD(RegisterDest(Register.x86(Register.x86Name.RSP)), ConstSrc(8L * pushedArgs)))
            }

            //get returns
            for (i in 1..n_returns.toInt()) {
                builder.add(cc.getReturn(i))
            }

            //remove padding
            if (didWePad) {
                builder.add(COMMENT("removing padding"))
                builder.add(Arith.ADD(RegisterDest(Register.x86(Register.x86Name.RSP)), ConstSrc(8L)))
            }

            //after everything is done, restore caller saved
            builder.add(CALLERSAVEPOP())
//            callerSavedRegs.reversed().forEach { builder.add(POP(it)) }
            return builder.build()
        }

    override fun findBestTile() {}
}