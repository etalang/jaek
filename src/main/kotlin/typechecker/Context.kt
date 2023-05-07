package typechecker

import typechecker.EtaType.ContextType

class Context {
    /** INVARIANT: "@" is always the name of the return variable. */
    var stack: ArrayList<MutableMap<String, ContextType>> = ArrayList()
    init {
        stack.add(HashMap())
    }

    fun enterScope() { // add a scope onto the stack
        stack.add(HashMap())
    }

    fun leaveScope() { // delete a scope from the stack
        stack.removeLast()
    }

    fun bind(id: String, type: ContextType) {
        stack.last()[id] = type
    }

    fun lookup(id: String): ContextType? {
        for (i in stack.size - 1 downTo 0) {
            val type = stack[i][id]
            if (type != null) {
                return type
            }
        }
        return null
    }

    fun recordTypes() : MutableSet<String> {
        val recordSet = mutableSetOf<String>()
        for (i in stack.size -1 downTo 0) {
            for (key in stack[i].keys) {
                val contextType = stack[i][key]
                if (contextType is ContextType.RecordType) {
                    recordSet.add(contextType.name)
                }
            }
        }
        return recordSet
    }
    fun contains(id: String): Boolean {
        return (lookup(id) != null)
    }

    fun functionMap(): Map<String, ContextType.FunType> {
        val contextFunctions: MutableMap<String, ContextType.FunType> = mutableMapOf()
        for (k in stack[0].keys) {
            val v = stack[0][k]
            if (v is ContextType.FunType) {
                contextFunctions[k] = v
            }
        }
        return contextFunctions
    }

    //TODO: factor out mangle function so we don't need to pass a reference
    fun runtimeFunctionMap(mangler: ((String, EtaType?) -> String)): Map<String, EtaFunc> {
        val funcMap: MutableMap<String, EtaFunc> = functionMap().mapKeys { (k, v) -> mangler(k, v) }.toMutableMap()
        funcMap["_eta_alloc"] = ContextType.FunType(
            EtaType.ExpandedType(arrayListOf(EtaType.OrdinaryType.IntType())),
            EtaType.ExpandedType(arrayListOf(EtaType.OrdinaryType.IntType())),
            true
        )
        funcMap["_eta_out_of_bounds"] =
            ContextType.FunType(EtaType.ExpandedType(arrayListOf()), EtaType.ExpandedType(arrayListOf()), true)
        return funcMap
    }


}