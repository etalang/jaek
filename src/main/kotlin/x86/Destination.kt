package x86

sealed class Destination {
    data class MemoryDest(val m:Memory) : Destination() {
        override fun toString(): String {
            return m.toString()
        }
    }

    data class RegisterDest(val r: Register) : Destination() {
        override fun toString(): String {
            return r.toString()
        }
    }

}
