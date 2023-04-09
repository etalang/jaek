package assembly.x86

sealed class Register {
    data class Abstract(val name : String) : Register() {
        override fun toString(): String {
            return name
        }
    }

    /** x86Name represents the register being used to store the information (may not be the whole register) */
    enum class x86Name {
        RAX, RBX, RCX, RDX, RSP, RBP, RDI, RSI, R8, R9, R10, R11, R12, R13, R14, R15
    }

    /** prints the corresponding name of the register */
    fun x86NametoString(n : x86Name) : String {
        return when(n) {
            x86Name.RAX -> "rax"
            x86Name.RBX -> "rbx"
            x86Name.RCX -> "rcx"
            x86Name.RDX -> "rdx"
            x86Name.RSP -> "rsp"
            x86Name.RBP -> "rbp"
            x86Name.RDI -> "rdi"
            x86Name.RSI -> "rsi"
            x86Name.R8 -> "r8"
            x86Name.R9 -> "r9"
            x86Name.R10 -> "r10"
            x86Name.R11 -> "r11"
            x86Name.R12 -> "r12"
            x86Name.R13 -> "r13"
            x86Name.R14 -> "r14"
            x86Name.R15 -> "r15"
        }
    }

    /** an instance of assembly represents a register in assembly.  */
    data class x86(val name : x86Name) : Register() {
        override fun toString(): String {
            return x86NametoString(name)
        }
    }

}
