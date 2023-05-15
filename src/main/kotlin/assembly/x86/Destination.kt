package assembly.x86

sealed class Destination {
    abstract val use : Set<Register>
    abstract val def : Set<Register>
    abstract val involved: Set<Register.Abstract>
    data class MemoryDest(val m: Memory) : Destination() {
        override val use: Set<Register> = m.involved
        override val def: Set<Register> = emptySet()
        override val involved: Set<Register.Abstract> = m.involved.filterIsInstance<Register.Abstract>().toSet()

        override fun toString(): String {
            return m.toString()
        }
    }

    data class RegisterDest(val r: Register) : Destination() {
        override val use: Set<Register> = setOf(r)
        override val def: Set<Register> = setOf(r)
        override val involved: Set<Register.Abstract> = listOf(r).filterIsInstance<Register.Abstract>().toSet()


        override fun toString(): String {
            return r.toString()
        }
    }

}
