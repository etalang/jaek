package ir.lowered

import assembly.tile.BuiltTile
import assembly.tile.TileBuilder
import assembly.x86.Destination
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.Source
import edu.cornell.cs.cs4120.etac.ir.IRBinOp

/** IROp(left,right) represents the evaluation of an arithmetic, logical, or relational
 * operation on the evaluated expressions of [left] and [right]**/
class LIROp(val op: IRBinOp.OpType, val left: LIRExpr, val right: LIRExpr) : LIRExpr() {
    override val java: IRBinOp = factory.IRBinOp(op, left.java, right.java)

    override val defaultTile : BuiltTile.ExprTile
    get() {
        val leftTile = left.optimalTile()
        val rightTile = right.optimalTile()
        return when (op) {
            IRBinOp.OpType.ADD -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister())
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Arith.ADD(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.SUB -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister())
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Arith.SUB(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.MUL -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister())
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Arith.MUL(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.HMUL -> TODO()
            IRBinOp.OpType.DIV -> TODO()
            IRBinOp.OpType.MOD -> TODO()
            IRBinOp.OpType.AND -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister())
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.AND(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.OR -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister())
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.OR(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.XOR -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister())
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.XOR(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.LSHIFT -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister())
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.SHL(
                        Destination.RegisterDest(builder.outputRegister),
                        Source.RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.RSHIFT -> TODO()
            IRBinOp.OpType.ARSHIFT -> TODO()
            IRBinOp.OpType.EQ -> TODO()
            IRBinOp.OpType.NEQ -> TODO()
            IRBinOp.OpType.LT -> TODO()
            IRBinOp.OpType.ULT -> TODO()
            IRBinOp.OpType.GT -> TODO()
            IRBinOp.OpType.LEQ -> TODO()
            IRBinOp.OpType.GEQ -> TODO()
        }
    }
    override fun findBestTile() {  }
}