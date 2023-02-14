package ast

sealed class Defn : Node {
    data class Method(val name : Expr, val args : ArrayList<Decl>, val returnTypes : ArrayList<Type>, val body : Stmt?)

    data class Global(val decl : Decl)
}
