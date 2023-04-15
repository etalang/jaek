package typechecker
import typechecker.EtaType.ContextType

class Context {
    /** INVARIANT: "@" is always the name of the return variable. */
    var stack : ArrayList<MutableMap<String, ContextType>> = ArrayList()
    init {
        stack.add(HashMap())
    }

    fun enterScope() { // add a scope onto the stack
        stack.add(HashMap())
    }

    fun leaveScope() { // delete a scope from the stack
        stack.removeLast()
    }

    fun bind(id : String, type : ContextType) {
        stack.last()[id] = type
    }

    fun lookup(id : String) : ContextType? {
        for (i in stack.size - 1 downTo 0) {
            val type = stack[i][id]
            if (type != null) {
                return type
            }
        }
        return null
    }

    fun contains(id: String): Boolean {
        return (lookup(id) != null)
    }

    fun getFunctions() : Map<String, ContextType.FunType> {
        val contextFunctions : MutableMap<String, ContextType.FunType> = mutableMapOf()
        for (k in stack[0].keys) {
            val v = stack[0][k]
            if (v is ContextType.FunType) {
                contextFunctions[k] = v
            }
        }
        return contextFunctions
    }



}