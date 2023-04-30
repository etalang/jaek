package optimize.cfg

sealed class CFGNode(
    val to: Edge
) {

    abstract val pretty: String

    class Gets(val varName: String, val expr: CFGExpr, edge: Edge) : CFGNode(edge) {
        override val pretty = "$varName ← ${expr.pretty}"
    }

    class Mem(val loc: CFGExpr, val expr: CFGExpr, edge: Edge) : CFGNode(edge) {
        override val pretty = "[${loc.pretty}] ← ${expr.pretty}"
    }

    //reallllly relying on some LIR invariants about how funciotns and procedures are always translated
    class Funcking(val name : String, val movIntos: List<String>, val args: List<CFGExpr>, edge: Edge) : CFGNode(edge) {
        override val pretty: String
            get() { //todo make less written shittility stupid
                (return if (movIntos.isEmpty()) "$name(${args.joinToString(", ") { it.pretty }})"
                else "${movIntos.joinToString(", ")} ← $name(${args.joinToString(", ") { it.pretty }})")
            }

    }

    class If(val cond: CFGExpr, val take: Edge, edge: Edge) : CFGNode(edge) {
        override val pretty = "if (${cond.pretty})"
    }

    class Start(val name: String, edge: Edge) : CFGNode(edge) {
        override val pretty = "start"
    }

    class Return(val rets: List<CFGExpr>, edge: Edge) : CFGNode(edge) {
        override val pretty = "return ${rets.joinToString(", ") { it.pretty }}"
    }

    class Cricket(to: Edge) : CFGNode(to) {
        override val pretty = "JUMP"
    }



}