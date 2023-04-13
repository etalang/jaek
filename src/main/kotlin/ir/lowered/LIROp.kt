package ir.lowered

import assembly.Tile
import assembly.TileBuilder
import assembly.x86.Destination
import assembly.x86.Destination.*
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.Source
import assembly.x86.Source.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp

/** IROp(left,right) represents the evaluation of an arithmetic, logical, or relational
 * operation on the evaluated expressions of [left] and [right]**/
class LIROp(val op: IRBinOp.OpType, val left: LIRExpr, val right: LIRExpr) : LIRExpr() {
    override val java: IRBinOp = factory.IRBinOp(op, left.java, right.java)

    override val defaultTile : Tile.Expr
    get() {
        val leftTile = left.optimalTile()
        val rightTile = right.optimalTile()
        return when (op) {
            IRBinOp.OpType.ADD -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Arith.ADD(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.SUB -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Arith.SUB(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.MUL -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Arith.MUL(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.HMUL -> TODO()
            IRBinOp.OpType.DIV -> TODO()
            IRBinOp.OpType.MOD -> TODO()
            IRBinOp.OpType.AND -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.AND(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.OR -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.OR(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.XOR -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.XOR(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.LSHIFT -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.SHL(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.RSHIFT -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.SHR(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.ARSHIFT -> {
                val builder = TileBuilder.Expr(2, Register.Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(
                    Instruction.MOV(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)
                    )
                )
                builder.add(
                    Instruction.Logic.SAR(
                        RegisterDest(builder.outputRegister),
                        RegisterSrc(rightTile.outputRegister)
                    )
                )
                builder.build()
            }
            IRBinOp.OpType.EQ -> {
                val outReg = Register.Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg)
                builder.add(Instruction.JumpSet.SETZ(eightByte))
                builder.build()
            }
            IRBinOp.OpType.NEQ -> {
                val outReg = Register.Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg)
                builder.add(Instruction.JumpSet.SETNZ(eightByte))
                builder.build()
            }
            IRBinOp.OpType.LT -> {
                val outReg = Register.Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg)
                builder.add(Instruction.JumpSet.SETL(eightByte))
                builder.build()
            }
            IRBinOp.OpType.ULT -> {
                val outReg = Register.Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg)
                builder.add(Instruction.JumpSet.SETB(eightByte))
                builder.build()
            }
            IRBinOp.OpType.GT -> {
                val outReg = Register.Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg)
                builder.add(Instruction.JumpSet.SETG(eightByte))
                builder.build()
            }
            IRBinOp.OpType.LEQ -> {
                val outReg = Register.Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg)
                builder.add(Instruction.JumpSet.SETLE(eightByte))
                builder.build()
            }
            IRBinOp.OpType.GEQ -> {
                val outReg = Register.Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg)
                builder.add(Instruction.JumpSet.SETGE(eightByte))
                builder.build()
            }
        }
    }

    private fun zeroAndCmp(builder : TileBuilder.Expr, leftTile : Tile.Expr, rightTile: Tile.Expr,
                   outReg : Register.Abstract) : Register {
        builder.consume(leftTile)
        builder.consume(rightTile)
        builder.add(
            Instruction.Logic.XOR(
                RegisterDest(builder.outputRegister),
                RegisterSrc(builder.outputRegister)
            )
        )
        builder.add(
            Instruction.CMP(
                leftTile.outputRegister,
                rightTile.outputRegister
            )
        )
        return Register.Abstract(outReg.name, 8)
    }
    override fun findBestTile() {  }
}