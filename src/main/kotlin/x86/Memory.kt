package x86

data class Memory(val base : Register, val index : Register?, val offset : Long = 0, val shift : Shift = Shift.ONE) {
    enum class Shift {
        ONE { override fun toString(): String { return "1" } },
        TWO { override fun toString(): String { return "2" } },
        FOUR { override fun toString(): String { return "4" } },
        EIGHT { override fun toString(): String { return "8" } }
    }

    @Override
    override fun toString(): String {
        var rep = "[$base"
        if (index != null) {
            rep += " + $index"
            if (shift != Shift.ONE) {
                rep += " * $shift"
            }
        }
        if (offset != 0L) {
            rep += " + $offset"
        }
        rep += "]"
        return rep
    }
}
