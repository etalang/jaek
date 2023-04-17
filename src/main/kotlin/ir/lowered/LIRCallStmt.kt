package ir.lowered

import assembly.ConventionalCaller
import assembly.Tile
import assembly.TileBuilder
import assembly.x86.*
import assembly.x86.Instruction.*
import assembly.x86.Destination.*
import assembly.x86.Source.*
import edu.cornell.cs.cs4120.etac.ir.IRCallStmt

class LIRCallStmt(val target: LIRExpr.LIRName, val n_returns: Long, val args: List<LIRExpr>) :
    LIRStmt.FlatStmt() {
    override val java: IRCallStmt = factory.IRCallStmt(target.java, n_returns, args.map { it.java })

    override val defaultTile : Tile.Regular =
        get() {
            val builder = TileBuilder.Regular(1, this)
            val cc = ConventionalCaller(args.size, n_returns.toInt())
        val reglst = mutableListOf<Register>()
        val argNumber = args.size
//        var argOffset = 0
//        for (arg in args) {
//            // ensures arguments are still evaluated left to right
//            val argTile = arg.optimalTile()
//            reglst.add(argTile.outputRegister)
//            builder.consume(argTile)
//        }

//  KEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEP
        if (n_returns>=3) {
            builder.add(Arith.SUB(RegisterDest(Register.x86(Register.x86Name.RSP)),
                    ConstSrc(8L * (n_returns - 2L))))
            builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.RDI)),
                    RegisterSrc(Register.x86(Register.x86Name.RSP))))
        }
//  END KEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEP



        if (argNumber > 6 - argOffset) {
            for (i in args.size - 1 downTo 6 - argOffset) {
                builder.add(PUSH(reglst[i]))
            }
        }
        if (argNumber > 5 - argOffset) {
            builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.R9)),
                RegisterSrc(reglst[5 - argOffset])))
        }
        if (argNumber > 4 - argOffset) {
            builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.R8)),
                    RegisterSrc(reglst[4 - argOffset])))
        }
        if (argNumber > 3 - argOffset) {
            builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.RCX)),
                    RegisterSrc(reglst[3 - argOffset])))
        }
        if (argNumber > 2 - argOffset) {
            builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.RDX)),
                RegisterSrc(reglst[2 - argOffset])))
        }
        if (argNumber > 1 - argOffset) {
            builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.RSI)),
                RegisterSrc(reglst[1 - argOffset])))
        }
        if (n_returns < 3 && argNumber > 0) {
            builder.add(MOV(RegisterDest(Register.x86(Register.x86Name.RDI)),
                    RegisterSrc(reglst.first())))
        }
        builder.add(CALL(Label(target.l, false)))
        if (argNumber > 6 - argOffset) {
            builder.add(Arith.ADD(RegisterDest(Register.x86(Register.x86Name.RSP)),
                    ConstSrc(8L * (argNumber - (6 - argOffset)))))
        }


            builder.add(MOV(RegisterDest(Register.Abstract("_RV1")),
                RegisterSrc(Register.x86(Register.x86Name.RAX))))
        if (n_returns >= 2) {
            builder.add(MOV(RegisterDest(Register.Abstract("_RV2")),
                    RegisterSrc(Register.x86(Register.x86Name.RDX))))
        }
        if (n_returns >= 3) {
            for (i in 3 .. n_returns.toInt()) {
                builder.add(POP(Register.Abstract("_RV$i")))
            }
        }
        return builder.build()
    }

    override fun findBestTile() {}
}