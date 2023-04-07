package assembly.x86

/** Labels as instructions refer only to labels that are referenced within function blocks. */
class Label(val name : String, val isTopLevel : Boolean) : Instruction() {
    override fun toString(): String {
        return if (isTopLevel) ".$name:"
        else name
    }
}