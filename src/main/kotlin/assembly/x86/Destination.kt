package assembly.x86

sealed class Destination {
    abstract val written : Set<Register>
    abstract val read : Set<Register>
    abstract val involved: Set<Register.Abstract>
    data class MemoryDest(val m: Memory) : Destination() {
        override val written: Set<Register> = emptySet()
        override val read: Set<Register> = m.involved
        override val involved: Set<Register.Abstract> = m.involved.filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return m.toString()
        }
    }

    data class RegisterDest(val r: Register) : Destination() {
        override val written: Set<Register> = setOf(r)
        override val read: Set<Register> = setOf(r)
        override val involved: Set<Register.Abstract> = listOf(r).filterIsInstance<Register.Abstract>().toSet()


        override fun toString(): String {
            return r.toString()
        }
    }

}
