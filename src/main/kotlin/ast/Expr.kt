package ast

sealed class Expr : Node {
    sealed class Binop {
        data class Arith(val op : String, val left : Expr, val right: Expr)
        data class Compare(val op : String, val left : Expr, val right: Expr)
    }

    sealed class Unop {
        data class IntNeg(val arg : Expr)
        data class BoolNeg(val arg : Expr)
    }

    data class Id(val name : String)

    data class FnCall (val fn : Expr, val args : ArrayList<Expr>)

    data class ArrayIdx (val arr : Expr, val idx : Expr)

    data class IntLit(val num : Long)

    data class StringLit(val text : String)

    enum class BoolLit { TRUE, FALSE }

    data class ArrayLit(val list : ArrayList<Expr>)
}
