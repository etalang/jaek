package ast

data class Decl(val id : Expr, val type : Type, val expr : Expr?) : Node