package assembly.x86

sealed class Source {
    abstract val use: Set<Register>
    abstract val involved: Set<Register.Abstract>


    data class MemorySrc(val m: Memory) : Source() {
        override val use: Set<Register> = m.involved
        override val involved: Set<Register.Abstract> = m.involved.filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return m.toString()
        }
    }

    data class RegisterSrc(val r: Register) : Source() {
        override val use: Set<Register> = setOf(r)
        override val involved: Set<Register.Abstract> = listOf(r).filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return r.toString()
        }
    }

    class ConstSrc(val c: Long) : Source() {
        override val use: Set<Register> = emptySet()
        override val involved: Set<Register.Abstract> = setOf()

        override fun toString(): String {
            return c.toString()
        }
    }
}
