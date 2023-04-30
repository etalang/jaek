package optimize.cfg

sealed class CFGNode(
    val to: Target
) {
//    constructor(map: Map<String, CFGNode>, nextId: String) : this(Target.Lazy(map, nextId))
//    constructor(next: CFGNode) : this(Target.Real(next))


    abstract val pretty: String
//    abstract val edges : List<Pair<CFGNode,String>>

    //    ₀₁₂₃₄₅₆₇₈
    class Gets(val varName: String, val expr: CFGExpr, target: Target) : CFGNode(target) {
        override val pretty = "$varName ← ${expr.pretty}"
    }

    class Mem(val loc: CFGExpr, val expr: CFGExpr, target: Target) : CFGNode(target) {
        override val pretty = "[${loc.pretty}] ← ${expr.pretty}"
    }

    //reallllly relying on some LIR invariants about how funciotns and procedures are always translated
    class Funcking(val name : String, val movIntos: List<String>, val args: List<CFGExpr>, target: Target) : CFGNode(target) {
        override val pretty: String
            get() { //todo make less written shittility stupid
                (return if (movIntos.isEmpty()) "$name(${args.joinToString(", ") { it.pretty }})"
                else "${movIntos.joinToString(", ")} ← $name(${args.joinToString(", ") { it.pretty }})")
            }

    }

    class If(val cond: CFGExpr, val take: Target, target: Target) : CFGNode(target) {
        override val pretty = "if (${cond.pretty})"
    }

    class Start(val name: String, target: Target) : CFGNode(target) {
        override val pretty = "start"
    }

    class Return(val rets: List<CFGExpr>, target: Target) : CFGNode(target) {
        override val pretty = "return ${rets.joinToString(", ") { it.pretty }}"
    }

    class Cricket(val take: Target) : CFGNode(take) {
        override val pretty = "CRICKET"
    }



}