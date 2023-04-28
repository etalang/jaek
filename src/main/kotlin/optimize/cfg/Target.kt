package optimize.cfg

sealed class Target {
    abstract val node: CFGNode?

    class Real(val n: CFGNode?) : Target() {
        override val node: CFGNode? = n
    }

    class Lazy(val map: Map<String, CFGNode>, val str: String) : Target() {
        override val node: CFGNode? get() = map[str]
    }

}