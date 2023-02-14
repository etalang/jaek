package ast

sealed class Stmt : Node {
    data class If (val guard : Expr, val thenBlock : Stmt, val elseBlock : Stmt?)

    data class While (val guard : Expr, val body : Stmt)

    data class Return (val args : ArrayList<Expr>)

    data class VarDecl(val id : Expr)

    data class Block(val stmts : ArrayList<Stmt>)

    data class CallProc(val id : Expr, val args : ArrayList<Expr>)



}
