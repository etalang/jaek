package typechecker

class Context {
    var stack : ArrayList<MutableMap<String, EtaType>> = ArrayList()
    init {
        stack.add(HashMap())
    }

    fun enterScope() { // add a scope onto the stack
        stack.add(HashMap())
    }

    fun leaveScope() { // delete a scope from the stack
        stack.removeLast()
    }

    fun bind(id : String, type : EtaType) {
        stack.last()[id] = type
    }

    fun lookup(id : String) : EtaType? {
        return stack.last()[id]
    }



}