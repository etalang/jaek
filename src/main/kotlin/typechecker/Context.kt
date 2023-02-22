package typechecker
import typechecker.EtaType.ContextType

class Context {
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
        return stack.last()[id]
    }



}