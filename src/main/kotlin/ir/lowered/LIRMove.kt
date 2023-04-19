package ir.lowered

import assembly.Tile
import assembly.TileBuilder
import assembly.x86.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRMove

/** IRMove represents moving the result of an expression to a destination**/
class LIRMove(val dest: LIRExpr, val expr: LIRExpr) : LIRStmt.FlatStmt() {
    override val java: IRMove = factory.IRMove(dest.java, expr.java)

    override val defaultTile: Tile.Regular
        get() {
            val builder = TileBuilder.Regular(1, this)
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

    /* Tiles moves in the form of MOVE(TEMP(t),CONST(0))) to XOR(TEMP(t),TEMP(t)) */
    private fun zeroTile() : Tile.Regular? {
        if (dest is LIRExpr.LIRTemp && expr is LIRExpr.LIRConst && expr.value == 0L) {
            val builder = TileBuilder.Regular(1, this)
            builder.add(
                Instruction.Logic.XOR(
                    Destination.RegisterDest(Register.Abstract(dest.name)),
                    Source.RegisterSrc(Register.Abstract(dest.name))
                )
            )
            return builder.build()
        }
        return null
    }

    // Takes the leaves of the LIROp and checks if they are dest and CONST(1)
    private fun isIncOrDec(left : LIRExpr, right : LIRExpr) : Boolean {
        if (left is LIRExpr.LIRConst && left.value == 1L && right == dest) {
            return true
        }
        if (right is LIRExpr.LIRConst && right.value == 1L && left == dest) {
            return true
        }
        return false
    }

    /* Tiles moves in the form of MOVE(Temp(t), ADD/SUB(Temp(t), CONST(1))) to INC/DEC(Temp(t)) */
    private fun incOrDec() : Tile.Regular? {
        if (dest is LIRExpr.LIRTemp && expr is LIROp) {

            if (expr.op == IRBinOp.OpType.ADD && isIncOrDec(expr.left, expr.right)) {
                val builder = TileBuilder.Regular(1, this)
                builder.add(
                    Instruction.Arith.INC(
                        Destination.RegisterDest(Register.Abstract(dest.name))
                    )
                )
            } else if (expr.op == IRBinOp.OpType.SUB && isIncOrDec(expr.left, expr.right)) {
                val builder = TileBuilder.Regular(1, this)
                builder.add(
                    Instruction.Arith.DEC(
                        Destination.RegisterDest(Register.Abstract(dest.name))
                    )
                )

            }
        }
        return null
    }
    // already have MOVE(TEMP(t1), MEM(e)) if e is memory-friendly

    private fun addressHack() : Tile.Regular? {
        if (dest is LIRExpr.LIRTemp) {
            val builder = TileBuilder.Regular(1, this)
            val smartAccess = detectMemoryFriendly(expr)
            if (smartAccess != null) {
                builder.add(Instruction.Arith.LEA(Destination.RegisterDest(Register.Abstract(dest.name)),
                    Source.MemorySrc(smartAccess)))
                return builder.build()
            }
            return null
        }
        return null
    }

    override fun findBestTile() {
        attempt(zeroTile())
        attempt(incOrDec())
        attempt(addressHack())
    }

}