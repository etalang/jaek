package assembly.x86

sealed class Destination {
    abstract val written : Set<Register>
    abstract val read : Set<Register>
    data class MemoryDest(val m: Memory) : Destination() {
        override val written: Set<Register> = emptySet()
        override val read: Set<Register> = m.involved

        override fun toString(): String {
            return m.toString()
        }
    }

    data class RegisterDest(val r: Register) : Destination() {
        override val written: Set<Register> = setOf(r)
        override val read: Set<Register> = setOf(r)

        override fun toString(): String {
            return r.toString()
        }
    }

}
