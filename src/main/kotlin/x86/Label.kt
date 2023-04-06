package x86

/** Labels as instructions refer only to labels that are referenced within function blocks. */
class Label(val name : String) : Instruction() {
    override fun toString(): String {
        return ".$name:"
    }
}