package assembly.x86

/** we should never instantiate this with both index and base null (or if both are null, offset needs to be non-negative) */
sealed class Memory {
    data class RegisterMem(val base: Register?, val index: Register?, val offset: Long = 0, val shift: Shift = Shift.ONE) : Memory() {
        init {
            require((base != null || index != null) || (offset >= 0))
        }
        enum class Shift {
            ONE {
                override fun toString(): String {
                    return "1"
                }
            },
            TWO {
                override fun toString(): String {
                    return "2"
                }
            },
            FOUR {
                override fun toString(): String {
                    return "4"
                }
            },
            EIGHT {
                override fun toString(): String {
                    return "8"
                }
            }
        }

        @Override
        override fun toString(): String {
            var rep = "QWORD PTR ["
            if (base != null) {
                rep += "$base"
            }
            if (index != null) {
                rep += " + $index"
                if (shift != Shift.ONE) {
                    rep += " * $shift"
                }
            }
            if (offset != 0L) {
                if (offset < 0) {
                    rep += " - ${-offset}"
                }
                else {
                    rep += " + $offset"
                }
            }
            rep += "]"
            return rep
        }
    }

    data class LabelMem(val label: Label) : Memory() {
        override fun toString(): String {
            return "[$label]"
        }
    }
}
