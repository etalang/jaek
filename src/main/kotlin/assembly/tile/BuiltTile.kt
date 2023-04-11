package assembly.tile

import assembly.x86.Instruction
import assembly.x86.Register

sealed class BuiltTile(val instructions: List<Instruction>, val cost: Int) {
    class ExprTile(instructions: List<Instruction>, cost: Int, val outputRegister: Register) :
        BuiltTile(instructions, cost)

    class RegularTile(instructions: List<Instruction>, cost: Int) : BuiltTile(instructions, cost)
}