package ir.lowered

import assembly.Tile
import assembly.TileBuilder
import assembly.x86.Destination.*
import assembly.x86.Instruction.*
import assembly.x86.Register
import assembly.x86.Register.*
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
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister), RegisterSrc(leftTile.outputRegister)))
                builder.add(Arith.ADD(RegisterDest(builder.outputRegister), RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.SUB -> {
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister), RegisterSrc(leftTile.outputRegister)))
                builder.add(Arith.SUB(RegisterDest(builder.outputRegister), RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.MUL -> {
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister), RegisterSrc(leftTile.outputRegister)))
                builder.add(Arith.MUL(RegisterDest(builder.outputRegister), RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.HMUL -> {
                val builder = TileBuilder.Expr(3, Abstract.freshRegister(),this)
                builder.add(MOV(RegisterDest(x86(x86Name.RAX)), RegisterSrc(rightTile.outputRegister)))
                builder.add(IMULSingle(leftTile.outputRegister))
                builder.add(MOV(RegisterDest(builder.outputRegister), RegisterSrc(x86(x86Name.RDX))))
                builder.build()
            }
            IRBinOp.OpType.DIV -> {
                val builder = TileBuilder.Expr(4, Abstract.freshRegister(),this)
                builder.add(MOV(RegisterDest(x86(x86Name.RAX)), RegisterSrc(leftTile.outputRegister)))
                builder.add(CQO())
                builder.add(DIV(rightTile.outputRegister))
                builder.add(MOV(RegisterDest(builder.outputRegister), RegisterSrc(x86(x86Name.RAX))))
                builder.build()
            }
            IRBinOp.OpType.MOD -> {
                val builder = TileBuilder.Expr(4, Abstract.freshRegister(),this)
                builder.add(MOV(RegisterDest(x86(x86Name.RAX)), RegisterSrc(leftTile.outputRegister)))
                builder.add(CQO())
                builder.add(DIV(rightTile.outputRegister))
                builder.add(MOV(RegisterDest(builder.outputRegister), RegisterSrc(x86(x86Name.RDX))))
                builder.build()
            }
            IRBinOp.OpType.AND -> {
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister),
                    RegisterSrc(leftTile.outputRegister)))
                builder.add(Logic.AND(RegisterDest(builder.outputRegister),
                    RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.OR -> {
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister),
                    RegisterSrc(leftTile.outputRegister)))
                builder.add(Logic.OR(RegisterDest(builder.outputRegister),
                    RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.XOR -> {
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister),
                    RegisterSrc(leftTile.outputRegister)))
                builder.add(Logic.XOR(RegisterDest(builder.outputRegister),
                    RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.LSHIFT -> {
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister),
                    RegisterSrc(leftTile.outputRegister)))
                builder.add(Logic.SHL(RegisterDest(builder.outputRegister),
                    RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.RSHIFT -> {
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister),
                    RegisterSrc(leftTile.outputRegister)))
                builder.add(Logic.SHR(RegisterDest(builder.outputRegister),
                    RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.ARSHIFT -> {
                val builder = TileBuilder.Expr(2, Abstract.freshRegister(),this)
                builder.consume(leftTile)
                builder.consume(rightTile)
                builder.add(MOV(RegisterDest(builder.outputRegister),
                        RegisterSrc(leftTile.outputRegister)))
                builder.add(Logic.SAR(RegisterDest(builder.outputRegister),
                    RegisterSrc(rightTile.outputRegister)))
                builder.build()
            }
            IRBinOp.OpType.EQ -> {
                val outReg = Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg.name)
                builder.add(JumpSet.SETZ(eightByte))
                builder.build()
            }
            IRBinOp.OpType.NEQ -> {
                val outReg = Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg.name)
                builder.add(JumpSet.SETNZ(eightByte))
                builder.build()
            }
            IRBinOp.OpType.LT -> {
                val outReg = Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg.name)
                builder.add(JumpSet.SETL(eightByte))
                builder.build()
            }
            IRBinOp.OpType.ULT -> {
                val outReg = Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg.name)
                builder.add(JumpSet.SETB(eightByte))
                builder.build()
            }
            IRBinOp.OpType.GT -> {
                val outReg = Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg.name)
                builder.add(JumpSet.SETG(eightByte))
                builder.build()
            }
            IRBinOp.OpType.LEQ -> {
                val outReg = Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg.name)
                builder.add(JumpSet.SETLE(eightByte))
                builder.build()
            }
            IRBinOp.OpType.GEQ -> {
                val outReg = Abstract.freshRegister()
                val builder = TileBuilder.Expr(3, outReg,this)
                val eightByte = zeroAndCmp(builder, leftTile, rightTile, outReg.name)
                builder.add(JumpSet.SETGE(eightByte))
                builder.build()
            }
        }
    }

    private fun zeroAndCmp(builder : TileBuilder.Expr, leftTile : Tile.Expr, rightTile: Tile.Expr,
                   outRegName : String) : Register {
        builder.consume(leftTile)
        builder.consume(rightTile)
        builder.add(Logic.XOR(RegisterDest(builder.outputRegister),
            RegisterSrc(builder.outputRegister)))
        builder.add(CMP(RegisterDest(leftTile.outputRegister),
            RegisterSrc(rightTile.outputRegister)))
        return Abstract(outRegName, 8)
    }
    override fun findBestTile() {  }
}