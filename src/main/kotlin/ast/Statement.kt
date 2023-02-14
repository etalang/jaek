package ast

sealed class Statement : Node {
    data class If (val guard : Expr, val thenBlock : Statement, val elseBlock : Statement?)

    data class While (val guard : Expr, val body : Statement)

    data class Return (val args : ArrayList<Expr>)

    data class VarDecl(val id : Expr)

    data class Block(val stmts : ArrayList<Statement>)

    data class CallProc(val id : Expr, val args : ArrayList<Expr>)

    data class Decl (val id : Expr, val type : Type)

}
