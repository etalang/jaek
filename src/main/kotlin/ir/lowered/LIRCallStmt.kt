package ir.lowered

import assembly.tile.BuiltTile
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRCallStmt

class LIRCallStmt(val target: LIRExpr.LIRName, val n_returns: Long, val args: List<LIRExpr>) :
    LIRStmt.FlatStmt() {
    override val java: IRCallStmt = factory.IRCallStmt(target.java, n_returns, args.map { it.java })

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
        insns.add(
            Instruction.Logic.AND(
                Destination.RegisterDest(Register.x86(Register.x86Name.RSP)),
                Source.ConstSrc(-16)
            ))
        if (n_returns >= 3) {
            argOffset = 1
            insns.add(
                Instruction.Arith.SUB(
                    Destination.RegisterDest(Register.x86(Register.x86Name.RSP)),
                    Source.ConstSrc(8L * (n_returns - 2L))
                )
            )
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
        if (n_returns < 3 && argNumber > 0) {
            insns.add(
                Instruction.MOV(
                    Destination.RegisterDest(Register.x86(Register.x86Name.RDI)),
                    Source.RegisterSrc(reglst.first())
                )
            )
        }
        insns.add(Instruction.CALL(Label(target.l, false)))
        if (argNumber > 6 - argOffset) {
            insns.add(
                Instruction.Arith.ADD(
                    Destination.RegisterDest(Register.x86(Register.x86Name.RSP)),
                    Source.ConstSrc(8L * (argNumber - (6 - argOffset)))
                )
            )
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