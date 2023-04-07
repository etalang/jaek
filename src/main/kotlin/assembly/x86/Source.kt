package assembly.x86

sealed class Source {
    data class MemorySrc(val m: Memory) : Source() {
        override fun toString(): String {
            return m.toString()
        }
    }

    data class RegisterSrc(val r: Register) : Source() {
        override fun toString(): String {
            return r.toString()
        }
    }

    class ConstSrc(val c : Long) : Source() {
        override fun toString(): String {
            return c.toString()
        }
    }
}
