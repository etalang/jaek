package ast

sealed class Definition : Node {
    data class Method(val name : Expr, val args : ArrayList<Statement>, val returnTypes : ArrayList<Type>, val body : Statement?)

    data class Global(val decl : Statement)
}
