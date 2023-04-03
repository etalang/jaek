package x86

sealed class Instruction {
    class MOV(val dest : Destination, val src : Source) : Instruction() {
        override fun toString(): String {
            return "mov $dest, $src" // this might require like a "QWORD PTR" somewhere
        }
    }

    sealed class Arith(val dest : Destination, val src : Source) : Instruction() {
        class ADD(dest : Destination, src : Source) : Arith(dest, src) {

        }

        class SUB(dest : Destination, src : Source) : Arith(dest, src)

        class MUL(dest : Destination, src : Source) : Arith(dest, src)

        class DIV(dest : Destination, src : Source) : Arith(dest, src)

        class LEA(dest : Destination, src : Source) : Arith(dest, src)

    }

    sealed class Logic(val dest : Destination, val src : Source) : Instruction() {
        class AND(dest : Destination, src : Source) : Logic(dest, src)

        class OR(dest : Destination, src : Source) : Logic(dest, src)

        class XOR(dest : Destination, src : Source) : Logic(dest, src)

        class SHL(dest : Destination, src : Source) : Logic(dest, src)

        class SHR(dest : Destination, src : Source) : Logic(dest, src)

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
    class RET : Instruction()

    class NOP : Instruction() {
        override fun toString(): String {
            return "nop"
        }
    }

}
