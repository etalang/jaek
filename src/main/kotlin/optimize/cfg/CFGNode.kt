package optimize.cfg

sealed class CFGNode(
    /** if you are not a `Lazy.Edge`, you should not be using lazyTo! */
    private val lazyTo: String?, val index: Int = nextNumber()
) {
    companion object {
        var index = 0
        fun nextNumber(): Int {
            return index++
        }
    }

    open fun resolveEdges(mm: MatchMaker) {
        if (lazyTo!=null)
        mm.build(this, lazyTo, false)
    }

    abstract val pretty: String

    override fun toString(): String {
        return pretty
    }

    sealed class Mov(edge: String) : CFGNode(edge) {
        abstract val target: String
    }

    class Gets(val varName: String, var expr: CFGExpr, private val to: String) : Mov(to) {
        override val pretty get() = "$varName ← ${expr.pretty}"
        override val target: String = varName
    }

    class Mem(var loc: CFGExpr, var expr: CFGExpr, edge: String) : Mov(edge) {
        override val pretty get() = "[${loc.pretty}] ← ${expr.pretty}"
        override val target: String = "[${loc.pretty}]"
    }

    //reallllly relying on some LIR invariants about how funciotns and procedures are always translated
    class Funcking(val name: String, val movIntos: List<Mov>, var args: List<CFGExpr>, edge: String) :
        CFGNode(edge) {
        override val pretty: String
            get() { //todo make less written shittility stupid
                (return if (movIntos.isEmpty()) "$name(${args.joinToString(", ") { it.pretty }})"
                else "${movIntos.joinToString(", ") { it.target }} ← $name(${args.joinToString(", ") { it.pretty }})")
            }

    }

    class If(var cond: CFGExpr, private val _take: String, edge: String) : CFGNode(edge) {
        var take: Edge? = null

        //TODO: be less stupid?????
        override val pretty
            get() =
                if (cond.pretty.startsWith("(") && cond.pretty.endsWith(")")) "if ${cond.pretty}" else "if (${cond.pretty})"

        override fun resolveEdges(mm: MatchMaker) {
            super.resolveEdges(mm)
            mm.build(this, _take, true)
        }
    }

    class Start(val name: String, edge: String) : CFGNode(edge) {
        override val pretty = "start"
    }

    class Return(var rets: List<CFGExpr>) : CFGNode(null) {
        override val pretty get() = "return ${rets.joinToString(", ") { it.pretty }}"
    }

    class Cricket(private val to: String) : CFGNode(to) {
        override val pretty = "JUMP"
        override fun resolveEdges(mm: MatchMaker) {
            mm.build(this, to, true)
        }
    }


}