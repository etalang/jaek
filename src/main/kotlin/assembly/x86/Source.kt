package assembly.x86

sealed class Source {
    abstract val read: Set<Register>

    data class MemorySrc(val m: Memory) : Source() {
        override val read: Set<Register> = m.involved

        override fun toString(): String {
            return m.toString()
        }
    }

    data class RegisterSrc(val r: Register) : Source() {
        override val read: Set<Register> = setOf(r)

        override fun toString(): String {
            return r.toString()
        }
    }

    class ConstSrc(val c: Long) : Source() {
        override val read: Set<Register> = emptySet()

        override fun toString(): String {
            return c.toString()
        }
    }
}
