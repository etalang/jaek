package ir.lowered

import assembly.tile.BuiltTile
import assembly.tile.TileBuilder
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRMove

/** IRMove represents moving the result of an expression to a destination**/
class LIRMove(val dest: LIRExpr, val expr: LIRExpr) : LIRStmt.FlatStmt() {
    override val java: IRMove = factory.IRMove(dest.java, expr.java)

    override val defaultTile: BuiltTile.RegularTile
        get() {
            val builder = TileBuilder.Regular(1)
            val srcTile = expr.optimalTile()
            builder.consume(srcTile)
            when (dest) {
                is LIRMem -> {
                    if (dest.address is LIRExpr.LIRName) { //TODO: maybe don't need this?
                        builder.add(
                            Instruction.MOV(
                                Destination.MemoryDest(Memory.LabelMem(Label(dest.address.l, false))),
                                Source.RegisterSrc(srcTile.outputRegister)
                            ),
                        )
                    } else {
                        val destTile = dest.address.optimalTile()
                        builder.consume(destTile)
                        builder.add(
                            Instruction.MOV(
                                Destination.MemoryDest(Memory.RegisterMem(destTile.outputRegister, null)),
                                Source.RegisterSrc(srcTile.outputRegister)
                            )
                        )
                    }
                }

                is LIRExpr.LIRTemp -> {
                    builder.add(
                        Instruction.MOV(
                            Destination.RegisterDest(Register.Abstract(dest.name)),
                            Source.RegisterSrc(srcTile.outputRegister)
                        )
                    )
                }

                else -> {
                    throw Exception("charles :(")
                }
            }

            return builder.build()
        }


    override fun findBestTile() {
        attempt(coolTiling())
    }

    private fun coolTiling(): BuiltTile.RegularTile? {
        return null
    }

}