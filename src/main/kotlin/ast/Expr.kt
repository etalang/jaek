package ast

sealed class Expr : Node() {
    //TODO finish
    class FnCall(val fn: Expr, val args: ArrayList<Expr>)

    class ArrayIdx(val arr: Expr, val idx: Expr)

}
