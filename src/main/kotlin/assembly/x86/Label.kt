package assembly.x86

/** Labels as instructions refer only to labels that are referenced within function blocks. */
class Label(val name: String, val isTopLevel: Boolean) : Instruction() {
    @Deprecated("") override val written: Set<Register> = setOf()
    @Deprecated("") override val read: Set<Register> = setOf()
    override val involved: Set<Register.Abstract> = setOf()

    override fun toString(): String {
        return if (isTopLevel) "$name:"
        else name
    }
}