package assembly.x86

/**
 * Flag Information:
 * - ZF = 1 <=> result = 0
 * - SF = 1 <=> result is (-)
 * - OF = 1 <=> result overflowed
 * - PF = 1 <=> sum of bits is even
 */
sealed class Instruction {
    @Deprecated("") abstract val written: Set<Register>
    @Deprecated("") abstract val read: Set<Register>
    @Deprecated("") val abstractEncountered: List<Register.Abstract> get() = (written union read).filterIsInstance<Register.Abstract>()
    @Deprecated("") val abstractWritten: List<Register.Abstract> get() = written.filterIsInstance<Register.Abstract>()
    @Deprecated("") val abstractRead: List<Register.Abstract> get() = read.filterIsInstance<Register.Abstract>()
    abstract val involved: Set<Register.Abstract>

    data class COMMENT(val str: String) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return "# $str"
        }
    }

    /**
     * copies [src] to [dest]
     *
     * The source operand can be an immediate value, general-purpose register, segment register, or memory location; the
     * destination register can be a general-purpose register, segment register, or memory location. Both operands must
     * be the same size, which can be a byte, a word, a doubleword, or a quadword.
     */
    data class MOV(val dest: Destination, val src: Source) : Instruction() {
        @Deprecated("") override val written: Set<Register> = dest.written
        @Deprecated("") override val read: Set<Register> = dest.read union src.read
        override val involved: Set<Register.Abstract> = dest.involved union src.involved

        override fun toString(): String {
            return "mov $dest, $src" // this might require like a "QWORD PTR" somewhere
        }
    }

    sealed class Arith(val dest: Destination, val src: Source) : Instruction() {
        @Deprecated("") override val written: Set<Register> = dest.written
        @Deprecated("") override val read: Set<Register> = dest.read union src.read
        override val involved: Set<Register.Abstract> = dest.involved union src.involved

        /**
         * [dest] := [dest] + [src]
         *
         * The ADD instruction performs integer addition. It evaluates the result for both signed and unsigned integer
         * operands and sets the OF and CF flags to indicate a carry (overflow) in the signed or unsigned result,
         * respectively. The SF flag indicates the sign of the signed result.
         */
        class ADD(dest: Destination, src: Source) : Arith(dest, src) {
            override fun toString(): String {
                return "add $dest, $src"
            }
        }

        /**
         * [dest] := [dest] - [src]
         *
         * The SUB instruction performs integer subtraction. It evaluates the result for both signed and unsigned
         * integer operands and sets the OF and CF flags to indicate an overflow in the signed or unsigned result,
         * respectively. The SF flag indicates the sign of the signed result.
         */
        class SUB(dest: Destination, src: Source) : Arith(dest, src) {
            override fun toString(): String {
                return "sub $dest, $src"
            }
        }

        /**
         * [dest] is multiplied by the [src]. The destination operand is a general-purpose register and the source
         * operand is an immediate value, a general-purpose register, or a memory location. The intermediate product
         * (twice the size of the input operand) is truncated and stored in the destination operand location.
         *
         * The CF and OF flags are set when the result must be truncated to fit in the destination operand size and
         * cleared when the result fits exactly in the destination operand size. The SF, ZF, AF, and PF flags are
         * undefined.
         */
        class MUL(dest: Destination, src: Source) : Arith(dest, src) {
            override fun toString(): String {
                return "imul $dest, $src"
            }
        }

        /**
         * Computes the effective address of the [src] and stores it [dest].
         *
         * The source operand is a memory address (offset part) specified with one of the processors addressing modes;
         * the destination operand is a general-purpose register.
         */
        class LEA(dest: Destination, src: Source.MemorySrc) : Arith(dest, src) {
            override fun toString(): String {
                return "lea $dest, $src"
            }

        }

        class INC(val dest: Destination) : Instruction() {
            @Deprecated("") override val written = dest.written
            @Deprecated("") override val read = dest.read
            override val involved: Set<Register.Abstract> = dest.involved

            override fun toString(): String {
                return "inc $dest"
            }
        }

        class DEC(val dest: Destination) : Instruction() {
            @Deprecated("") override val written = dest.written
            @Deprecated("") override val read = dest.read
            override val involved: Set<Register.Abstract> = dest.involved

            override fun toString(): String {
                return "dec $dest"
            }
        }

    }

    sealed class Logic(var dest: Destination, var src: Source) : Instruction() {
        @Deprecated("") override val written: Set<Register> = dest.written
        @Deprecated("") override val read: Set<Register> = dest.read union src.read
        override val involved: Set<Register.Abstract> = dest.involved union src.involved

        /**
         * [dest] := [dest] AND [src]
         *
         * The OF and CF flags are cleared; the SF, ZF, and PF flags are set according to the result. The state of the
         * AF flag is undefined.
         */
        class AND(dest: Destination, src: Source) : Logic(dest, src) {
            override fun toString(): String {
                return "and $dest, $src"
            }
        }

        /**
         * [dest] := [dest] OR [src]
         *
         * The OF and CF flags are cleared; the SF, ZF, and PF flags are set according to the result.
         */
        class OR(dest: Destination, src: Source) : Logic(dest, src) {
            override fun toString(): String {
                return "or $dest, $src"
            }
        }

        /**
         * [dest] := [dest] XOR [src]
         *
         * The OF and CF flags are cleared; the SF, ZF, and PF flags are set according to the result.
         */
        class XOR(dest: Destination, src: Source) : Logic(dest, src) {
            override fun toString(): String {
                return "xor $dest, $src"
            }
        }

        /**
         * The CF flag contains the value of the last bit shifted out of the destination operand; it is undefined for
         * SHL and SHR instructions where the count is greater than or equal to the size (in bits) of the destination
         * operand. The OF flag is affected only for 1-bit shifts; otherwise, it is undefined. The SF, ZF, and PF flags
         * are set according to the result. If the count is 0, the flags are not affected. For a non-zero count, the AF
         * flag is undefined.
         */
        class SHL(dest: Destination, src: Source) : Logic(dest, src) {
            override fun toString(): String {
                return "shl $dest, $src"
            }
        }

        /**
         * The CF flag contains the value of the last bit shifted out of the destination operand; it is undefined for
         * SHL and SHR instructions where the count is greater than or equal to the size (in bits) of the destination
         * operand. The OF flag is affected only for 1-bit shifts; otherwise, it is undefined. The SF, ZF, and PF flags
         * are set according to the result. If the count is 0, the flags are not affected. For a non-zero count, the AF
         * flag is undefined.
         */
        class SHR(dest: Destination, src: Source) : Logic(dest, src) {
            override fun toString(): String {
                return "shr $dest, $src"
            }
        }

        /**
         * The CF flag contains the value of the last bit shifted out of the destination operand; it is undefined for
         * SHL and SHR instructions where the count is greater than or equal to the size (in bits) of the destination
         * operand. The OF flag is affected only for 1-bit shifts; otherwise, it is undefined. The SF, ZF, and PF flags
         * are set according to the result. If the count is 0, the flags are not affected. For a non-zero count, the AF
         * flag is undefined.
         */
        class SAR(dest: Destination, src: Source) : Logic(dest, src) {
            override fun toString(): String {
                return "sar $dest, $src"
            }
        }

    }

    // TODO: fix the typing on these instructions to be correct (see manual)
    // the arity is correct, but the types are hella wrong below:
    /**
     * temp := [dest] − SignExtend([src]);
     *
     * The CF, OF, SF, ZF, AF, and PF flags are set according to the result.
     */
    data class CMP(val dest: Destination, val src: Source) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = dest.read union src.read
        override val involved: Set<Register.Abstract> = dest.involved union src.involved

        override fun toString(): String {
            return "cmp $dest, $src"
        }
    }

    /**
     * TEMP := [reg1] AND [reg2];
     *
     * SF := MSB(TEMP); IF TEMP = 0 THEN ZF := 1; ELSE ZF := 0; FI: PF := BitwiseXNOR(TEMP[0:7]); CF := 0; OF := 0; (*
     * AF is undefined *)
     */
    data class TEST(val reg1: Register, val reg2: Register) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf(reg1, reg2)
        override val involved: Set<Register.Abstract> = listOf(reg1, reg2).filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return "test $reg1, $reg2"
        }
    }

    /**
     * sets the low 8 bits of register with 0 or 1 if the corresponding jump would be taken
     *
     * @param reg where size must be 8
     */
    sealed class JumpSet(val reg: Register) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf(reg)
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = listOf(reg).filterIsInstance<Register.Abstract>().toSet()

        class SETZ(reg: Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setz $reg"
            }
        }

        class SETNZ(reg: Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setnz $reg"
            }
        }

        class SETL(reg: Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setl $reg"
            }
        }

        class SETLE(reg: Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setle $reg"
            }
        }

        /** Specifically for unsigned jump JB (below) */
        class SETB(reg: Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setb $reg"
            }
        }

        class SETG(reg: Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setg $reg"
            }
        }

        class SETGE(reg: Register) : JumpSet(reg) {
            override fun toString(): String {
                return "setge $reg"
            }
        }

    }

    sealed class Jump(val loc: Location) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        /** Transfers program control to instruction at [loc] */
        class JMP(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jmp $loc"
            }
        }

        /** Jump if **ZF = 1** to [loc] */
        class JE(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "je $loc"
            }
        }

        /** Jump if **ZF = 0** to [loc] */
        class JNE(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jne $loc"
            }
        }

        /** Jump if **SF ≠ OF** to [loc] */
        class JL(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jl $loc"
            }
        }

        /** Jump if **ZF=1 or SF ≠ OF** to [loc] */
        class JLE(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jle $loc"
            }
        }

        /** Jump if **ZF = 0 and SF = OF** to [loc] */
        class JG(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jg $loc"
            }
        }

        /** Jump if **SF = OF** to [loc] */
        class JGE(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jge $loc"
            }
        }

        /** Jump if **CF = 1** */
        class JB(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jb $loc"
            }
        }

        /** Jump if **ZF = 1** */
        class JZ(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jz $loc"
            }
        }

        /** Jump if **ZF = 0** */
        class JNZ(loc: Location) : Jump(loc) {
            override fun toString(): String {
                return "jnz $loc"
            }
        }
    }

    /** RDX:RAX := sign-extend of RAX */
    class CQO : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return "cqo"
        }
    }

    /** Decrements the stack pointer and then stores [arg] on the top of the stack. */
    data class PUSH(val arg: Register) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf(arg)
        override val involved: Set<Register.Abstract> = listOf(arg).filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return "push $arg"
        }
    }

    /** Decrements the stack pointer and then stores 204 on the top of the stack. */
    class PAD() : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return "push 204"
        }
    }


    /**
     * Loads the value from the top of the stack to [dest] and then increments the stack pointer.
     *
     * TODO: The destination operand can be a general-purpose register, memory location, or segment register.
     */
    data class POP(val dest: Register) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf(dest)
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = listOf(dest).filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return "pop $dest"
        }
    }

    /**
     * Saves procedure linking information on the stack and branches to the called procedure specified using the target
     * operand. [label] specifies the address of the first instruction in the called procedure.
     *
     * TODO: The operand can be an immediate value, a general-purpose register, or a memory location.
     */
    class CALL(val label: Label) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return "call $label"
        }
    }

    /**
     * Creates a stack frame for a procedure. [bytes] specifies the size of the dynamic storage in the stack frame (that
     * is, the number of bytes of dynamically allocated on the stack for the procedure).
     *
     * TODO: The second operand (imm8) gives the lexical nesting level (0 to 31) of the procedure. The nesting level
     *     (imm8 mod 32) and the OperandSize attribute determine the size in bytes of the storage space for frame
     *     pointers.
     */
    class ENTER(val bytes: Long) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return "enter $bytes, 0"
        }
    }

    /**
     * Releases the stack frame set up by an earlier ENTER instruction.
     *
     * Copies the frame pointer into the stack pointer register, which releases the stack space allocated to the
     * stack frame. The old frame pointer (the frame pointer for the calling procedure that was saved by the ENTER
     * instruction) is then popped from the stack into the EBP register, restoring the calling procedure’s stack frame.
     */
    class LEAVE : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return "leave"
        }
    }

    /**
     * Transfers program control to a return address located on the top of the stack. The address is usually placed on
     * the stack by a CALL instruction, and the return is made to the instruction that follows the CALL instruction.
     */
    class RET : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return "ret"
        }
    }

    /** Jaek */
    class NOP : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf()
        @Deprecated("") override val read: Set<Register> = setOf()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return "nop"
        }
    }

    /** Unsigned divide RDX:RAX by [divisor], with result stored in RAX := Quotient, RDX := Remainder */
    class DIV(val divisor: Register) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf(
            Register.x86(Register.x86Name.RAX), Register.x86(Register.x86Name.RDX)
        )
        @Deprecated("") override val read: Set<Register> = setOf(divisor)
        override val involved: Set<Register.Abstract> = listOf(divisor).filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return "idiv $divisor"
        }
    }

    /** RDX:RAX := RAX * [factor]. */
    class IMULSingle(val factor: Register) : Instruction() {
        @Deprecated("") override val written: Set<Register> = setOf(
            Register.x86(Register.x86Name.RAX), Register.x86(Register.x86Name.RDX)
        )
        @Deprecated("") override val read: Set<Register> = setOf(factor)
        override val involved: Set<Register.Abstract> = listOf(factor).filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return "imul $factor"
        }
    }

}
