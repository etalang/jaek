package assembly.x86

/** Labels as instructions refer only to labels that are referenced within function blocks. */
class Label(val name: String, val isTopLevel: Boolean) : Instruction() {
    override val involved: Set<Register.Abstract> = setOf()
    override val use: Set<Register> = setOf()
    override val def: Set<Register> = setOf()

    override fun toString(): String {
        return if (isTopLevel) "$name:"
        else name
    }
}