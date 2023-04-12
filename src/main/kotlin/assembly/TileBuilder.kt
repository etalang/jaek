package assembly

import assembly.x86.Instruction
import assembly.x86.Register
import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter
import ir.lowered.LIRNode
import java.io.ByteArrayOutputStream

sealed class TileBuilder<TileType>(private val baseCost: Int, private val source: LIRNode) where TileType : Tile {
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

    fun consume(tile: Tile) {
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
    class Regular(baseCost: Int, source: LIRNode) : TileBuilder<Tile.Regular>(baseCost, source) {
        override fun build(): Tile.Regular {
            return Tile.Regular(instructions, cost)
        }
    }

    class Expr(baseCost: Int, val outputRegister: Register, source: LIRNode) :
        TileBuilder<Tile.Expr>(baseCost, source) {
        override fun build(): Tile.Expr {
            return Tile.Expr(instructions, cost, outputRegister)
        }
    }
}