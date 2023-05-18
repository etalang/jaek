package assembly.x86

sealed class Register {
    data class Abstract(val name: String, val size : Int = 64) : Register() {
        override fun toString(): String {
            return name
        }

        override fun equals(other: Any?): Boolean {
            return if (other is Abstract){
                other.name == this.name
            } else false
        }

        companion object {
            private var freshRegisterCount = 0
            fun freshRegister(): Abstract {
                freshRegisterCount++
                return Abstract("\$A$freshRegisterCount")
            }
        }
    }

        /** x86Name represents the register being used to store the information (may not be the whole register) */
    enum class x86Name {
        RAX, RBX, RCX, RDX, RSP, RBP, RDI, RSI, R8, R9, R10, R11, R12, R13, R14, R15
    }

    /** prints the corresponding name of the register */
    fun x86NametoString(n: x86Name): String {
        return when (n) {
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

    fun x86NametoLow8BitString(n: x86Name): String {
        return when (n) {
            x86Name.RAX -> "al"
            x86Name.RBX -> "bl"
            x86Name.RCX -> "cl"
            x86Name.RDX -> "dl"
            x86Name.RSP -> "spl"
            x86Name.RBP -> "bpl"
            x86Name.RDI -> "dil"
            x86Name.RSI -> "sil"
            x86Name.R8 -> "r8b"
            x86Name.R9 -> "r9b"
            x86Name.R10 -> "r10b"
            x86Name.R11 -> "r11b"
            x86Name.R12 -> "r12b"
            x86Name.R13 -> "r13b"
            x86Name.R14 -> "r14b"
            x86Name.R15 -> "r15b"
        }
    }

    /** an instance of assembly represents a register in assembly.  */
    data class x86(val name: x86Name, val size : Int = 64) : Register() {
        override fun toString(): String {
            return if (size == 64) x86NametoString(name) else x86NametoLow8BitString(name)
        }

        companion object {
            fun calleeSaved() : Set<x86> {
                return setOf(
                x86(x86Name.RBX),
                x86(x86Name.R12),
                x86(x86Name.R13),
                x86(x86Name.R14),
                x86(x86Name.R15))
            }
            fun callerSaved() : Set<x86> {
                return setOf(x86(x86Name.RAX),
                    x86(x86Name.RCX),
                    x86(x86Name.RDX),
                    x86(x86Name.RDI),
                    x86(x86Name.RSI),
                    x86(x86Name.R8),
                    x86(x86Name.R9),
                    x86(x86Name.R10),
                    x86(x86Name.R11))
            }
        }

        override fun equals(other: Any?): Boolean {
            return if (other is x86){
                other.name == this.name
            } else false
        }
    }

}
