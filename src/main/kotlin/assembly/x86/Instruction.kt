package assembly.x86

sealed class Instruction {
    class MOV(val dest : Destination, val src : Source) : Instruction() {
        override fun toString(): String {
            return "mov $dest, $src" // this might require like a "QWORD PTR" somewhere
        }
    }

    sealed class Arith(val dest : Destination, val src : Source) : Instruction() {
        class ADD(dest : Destination, src : Source) : Arith(dest, src) {
            override fun toString(): String {
                return "add $dest, $src"
            }
        }

        class SUB(dest : Destination, src : Source) : Arith(dest, src) {
            override fun toString(): String {
                return "sub $dest, $src"
            }
        }

        class MUL(dest : Destination, src : Source) : Arith(dest, src)

        class DIV(dest : Destination, src : Source) : Arith(dest, src)

        class LEA(dest : Destination, src : Source) : Arith(dest, src)

    }

    sealed class Logic(val dest : Destination, val src : Source) : Instruction() {
        class AND(dest : Destination, src : Source) : Logic(dest, src) {
            override fun toString(): String {
                return "and $dest, $src"
            }
        }

        class OR(dest : Destination, src : Source) : Logic(dest, src) {
            override fun toString(): String {
                return "or $dest, $src"
            }
        }

        class XOR(dest : Destination, src : Source) : Logic(dest, src) {
            override fun toString(): String {
                return "xor $dest, $src"
            }
        }

        class SHL(dest : Destination, src : Source) : Logic(dest, src)

        class SHR(dest : Destination, src : Source) : Logic(dest, src)

    }

    // TODO: fix the typing on these instructions to be correct (see manual)
    // the arity is correct, but the types are hella wrong below:
    class CMP(val reg1 : Register, val reg2 : Register) : Instruction()

    class TEST(val reg1: Register, val reg2: Register) : Instruction() {
        override fun toString(): String {
            return "test $reg1, $reg2"
        }
    }

    sealed class Jump(val loc: Location) : Instruction() {
        class JMP(loc : Location) : Jump(loc) {
            override fun toString(): String {
                return "jmp $loc"
            }
        }
        class JE(loc : Location) : Jump(loc) {
            override fun toString(): String {
                return "je $loc"
            }
        }
        // TODO: implement toString for all other jumps

        class JNE(loc: Location) : Jump(loc)
        class JL(loc: Location) : Jump(loc)
        class JLE(loc: Location) : Jump(loc)

        class JG(loc: Location) : Jump(loc)
        class JGE(loc: Location) : Jump(loc)

        class JZ(loc: Location) : Jump(loc)
        class JNZ(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jnz $loc"
            }
        }
    }

    class PUSH(val arg : Register) : Instruction() {
        override fun toString(): String {
            return "push $arg"
        }
    }

    class POP(val dest : Register) : Instruction() {
        override fun toString(): String {
            return "pop $dest"
        }
    }

    class CALL(val label : Label) : Instruction() {
        override fun toString(): String {
            return "call $label"
        }
    }

    class ENTER(val bytes : Long) : Instruction() {
        override fun toString(): String {
            return "enter $bytes, 0"
        }
    }

    class LEAVE : Instruction() {
        override fun toString(): String {
            return "leave"
        }
    }

    class RET : Instruction() {
        override fun toString(): String {
            return "ret"
        }
    }

    class NOP : Instruction() {
        override fun toString(): String {
            return "nop"
        }
    }

}
