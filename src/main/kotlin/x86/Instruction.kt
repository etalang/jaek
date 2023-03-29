package x86

sealed class Instruction {
    class MOV(val d : Destination, val s : Source) : Instruction() {
        override fun toString(): String {
            return "mov $d, $s" // this might require like a "QWORD PTR" somewhere
        }
    }

    // TODO: think about organization of arithmetic/logical operations -- should it follow how jumps are organized below?
    // don't really know how to organize arithmetic/logic insns
    enum class ArithType {
        ADD, SUB, MUL, DIV, INC, DEC, LEA
    }
    sealed class Arith(val type : ArithType, val dest : Destination, val operand : Source ) : Instruction() {

    }

    enum class LogicType {
        AND, OR, NOT, XOR, SHL, SHR
    }

    sealed class Logic(val type : LogicType, val dest : Destination, val operand : Source) : Instruction() {

    }

    // TODO: fix the typing on these instructions to be correct (see manual)
    // the arity is correct, but the types are hella wrong below:
    class CMP(val reg1 : Register, val reg2 : Register) : Instruction()

    class TEST(val reg1: Register, val reg2: Register) : Instruction()

    sealed class JMP(val reg: Register) : Instruction() {
        class JE(reg : Register) : JMP(reg) {
            override fun toString(): String {
                return "je $reg"
            }
        }
        // TODO: implement toString for all other jumps

        class JNE(reg: Register) : JMP(reg)
        class JL(reg: Register) : JMP(reg)
        class JLE(reg: Register) : JMP(reg)

        class JG(reg: Register) : JMP(reg)
        class JGE(reg: Register) : JMP(reg)

        class JZ(reg: Register) : JMP(reg)
        class JNZ(reg: Register) : JMP(reg)

    }

    class PUSH(val arg : Register) : Instruction()

    class POP(val dest : Register) : Instruction()

    class CALL(val label : Label) : Instruction() // ?

    // TODO: are these objects OK??
    object RET : Instruction()

    object NOP : Instruction() {
        override fun toString(): String {
            return "nop"
        }
    }

}
