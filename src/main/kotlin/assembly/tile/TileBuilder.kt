package assembly.tile

import assembly.x86.Instruction
import assembly.x86.Register
import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter
import ir.lowered.LIRNode
import java.io.ByteArrayOutputStream

sealed class TileBuilder<TileType>(private val baseCost: Int, private val source: LIRNode) where TileType : BuiltTile {
    protected val instructions: MutableList<Instruction>

    init {
        val baos = ByteArrayOutputStream()
        val writer = CodeWriterSExpPrinter(baos)
        source.java.printSExp(writer)
        writer.flush()
        writer.close()
        instructions = mutableListOf(Instruction.COMMENT("[IR] ${baos.toString().replace("\n", "")}"))
    }

    protected var cost = baseCost
    val publicIns: List<Instruction> get() = instructions
    val publicCost: Int get() = cost

    fun consume(tile: BuiltTile) {
        instructions.addAll(tile.instructions)
        cost += tile.cost
    }

    fun add(instructions: List<Instruction>) {
        this.instructions.addAll(instructions)
    }

    fun add(instruction: Instruction) {
        this.instructions.add(instruction)
    }

    abstract fun build(): TileType
    class Regular(baseCost: Int, source: LIRNode) : TileBuilder<BuiltTile.RegularTile>(baseCost, source) {
        override fun build(): BuiltTile.RegularTile {
            return BuiltTile.RegularTile(instructions, cost)
        }
    }

    class Expr(baseCost: Int, val outputRegister: Register, source: LIRNode) :
        TileBuilder<BuiltTile.ExprTile>(baseCost, source) {
        override fun build(): BuiltTile.ExprTile {
            return BuiltTile.ExprTile(instructions, cost, outputRegister)
        }
    }
}