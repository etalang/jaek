package assembly.x86

sealed class Instruction {

    data class COMMENT(val str: String) : Instruction() {
        override fun toString(): String {
            return "# $str"
        }
    }

    data class MOV(val dest : Destination, val src : Source) : Instruction() {
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

        class MUL(dest : Destination, src : Source) : Arith(dest, src) {
            override fun toString(): String {
                return "imul $dest, $src"
            }
        }

        class DIV(dest : Destination, src : Source) : Arith(dest, src)

        class LEA(dest : Destination, src : Source) : Arith(dest, src)

    }

    sealed class Logic(var dest : Destination, var src : Source) : Instruction() {
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

        class SHL(dest : Destination, src : Source) : Logic(dest, src) {
            override fun toString(): String {
                return "shl $dest, $src"
            }
        }

        class SHR(dest : Destination, src : Source) : Logic(dest, src) {
            override fun toString(): String {
                return "shr $dest, $src"
            }
        }

        class SAR(dest : Destination, src : Source) : Logic(dest, src) {
            override fun toString(): String {
                return "sar $dest, $src"
            }
        }

    }

    // TODO: fix the typing on these instructions to be correct (see manual)
    // the arity is correct, but the types are hella wrong below:
    data class CMP(val reg1 : Register, val reg2 : Register) : Instruction() {
        override fun toString(): String {
            return "cmp $reg1, $reg2"
        }
    }

    data class TEST(val reg1: Register, val reg2: Register) : Instruction() {
        override fun toString(): String {
            return "test $reg1, $reg2"
        }
    }

    /** sets the low 8 bits of register with 0 or 1 if the corresponding jump would be taken
     * @param register where size must be 8 */
    sealed class JumpSet(val reg : Register) : Instruction() {
        class SETZ(reg : Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setz $reg"
            }
        }

        class SETNZ(reg : Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setz $reg"
            }
        }

        class SETL(reg : Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setl $reg"
            }
        }

        class SETLE(reg : Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setle $reg"
            }
        }

        /**
         * Specifically for unsigned jump JB (below)
         */
        class SETB(reg : Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setb $reg"
            }
        }

        class SETG(reg : Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setg $reg"
            }
        }
        class SETGE(reg : Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setge $reg"
            }
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

        class JNE(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jne $loc"
            }
        }
        class JL(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jl $loc"
            }
        }
        class JLE(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jle $loc"
            }
        }

        class JG(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jg $loc"
            }
        }
        class JGE(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jge $loc"
            }
        }

        class JZ(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jz $loc"
            }
        }
        class JNZ(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jnz $loc"
            }
        }
    }

    data class PUSH(val arg : Register) : Instruction() {
        override fun toString(): String {
            return "push $arg"
        }
    }

    data class POP(val dest : Register) : Instruction() {
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
