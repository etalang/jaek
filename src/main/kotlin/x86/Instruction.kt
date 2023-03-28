package x86

sealed class Instruction {
    sealed class MOV(val d : Destination, val s : Source) : Instruction() {
        override fun toString(): String {
            return "mov $d, $s"
        }

    }

    enum class ArithType {
        ADD, SUB, IMUL, DIV,
    }
    data class Arith(val type : ArithType, val dest : Memory, val operand : String ) : Instruction() {

    }

}
