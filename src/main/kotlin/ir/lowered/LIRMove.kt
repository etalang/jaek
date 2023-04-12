package ir.lowered

import assembly.tile.BuiltTile
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRMove

/** IRMove represents moving the result of an expression to a destination**/
class LIRMove(val dest: LIRExpr, val expr: LIRExpr) : LIRStmt.FlatStmt() {
    override val java: IRMove = factory.IRMove(dest.java, expr.java)

    override val defaultTile: BuiltTile.RegularTile
        get() = expr.optimalTile().let { srcTile ->
            when (dest) {
                is LIRMem -> {
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