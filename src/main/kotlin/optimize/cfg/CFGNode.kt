package optimize.cfg

sealed class CFGNode(
    /** if you are not a `Lazy.Edge`, you should not be using this! */
    val lazyTo: Edge.Lazy, var to: Edge? = null
) {
    open fun resolveEdges() {
        to = lazyTo.toReal()
    }

    open val edges: Set<Edge> get() = setOfNotNull(to)

    abstract val pretty: String

    sealed class Mov(edge: Edge.Lazy) : CFGNode(edge) {
        abstract val target: String
    }

    class Gets(val varName: String, val expr: CFGExpr, edge: Edge.Lazy) : Mov(edge) {
        override val pretty = "$varName ← ${expr.pretty}"
        override val target: String = varName
    }

    class Mem(val loc: CFGExpr, val expr: CFGExpr, edge: Edge.Lazy) : Mov(edge) {
        override val pretty = "[${loc.pretty}] ← ${expr.pretty}"
        override val target: String = "[${loc.pretty}]"
    }

    //reallllly relying on some LIR invariants about how funciotns and procedures are always translated
    class Funcking(val name: String, val movIntos: List<Mov>, val args: List<CFGExpr>, edge: Edge.Lazy) :
        CFGNode(edge) {
        override val pretty: String
            get() { //todo make less written shittility stupid
                (return if (movIntos.isEmpty()) "$name(${args.joinToString(", ") { it.pretty }})"
                else "${movIntos.joinToString(", ") { it.target }} ← $name(${args.joinToString(", ") { it.pretty }})")
            }

    }

    class If(val cond: CFGExpr, val _take: Edge.Lazy, edge: Edge.Lazy) : CFGNode(edge) {
        var take: Edge? = null
        override val pretty = "if (${cond.pretty})"
        override fun resolveEdges() {
            super.resolveEdges()
            take = _take.toReal(true)
        }

        override val edges: Set<Edge>
            get() = super.edges.plus(setOfNotNull(take))
    }

    class Start(val name: String, edge: Edge.Lazy) : CFGNode(edge) {
        override val pretty = "start"
    }

    class Return(val rets: List<CFGExpr>, edge: Edge.Lazy) : CFGNode(edge) {
        override val pretty = "return ${rets.joinToString(", ") { it.pretty }}"
    }

    class Cricket(to: Edge.Lazy) : CFGNode(to) {
        override val pretty = "JUMP"
    }


}