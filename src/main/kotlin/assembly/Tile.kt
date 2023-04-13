package assembly

import assembly.x86.Instruction
import assembly.x86.Register

sealed class Tile(val instructions: List<Instruction>, val cost: Int) {
    class Expr(instructions: List<Instruction>, cost: Int, val outputRegister: Register) :
        Tile(instructions, cost)

    class Regular(instructions: List<Instruction>, cost: Int) : Tile(instructions, cost)
}