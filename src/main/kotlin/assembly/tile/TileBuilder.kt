package assembly.tile

import assembly.x86.Instruction
import assembly.x86.Register

sealed class TileBuilder<TileType>(private val baseCost: Int) where TileType : BuiltTile {
    protected val instructions: MutableList<Instruction> = mutableListOf()
    protected var cost = baseCost
    val publicIns : List<Instruction> get() = instructions
    val publicCost : Int get() = cost

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
    class Regular(baseCost: Int) : TileBuilder<BuiltTile.RegularTile>(baseCost) {
        override fun build(): BuiltTile.RegularTile {
            return BuiltTile.RegularTile(instructions, cost)
        }
    }

    class Expr(baseCost: Int, val outputRegister: Register) : TileBuilder<BuiltTile.ExprTile>(baseCost) {
        override fun build(): BuiltTile.ExprTile {
            return BuiltTile.ExprTile(instructions, cost, outputRegister)
        }
    }
}