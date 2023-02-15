package ast

class MultiAssign (
    val targets : ArrayList<AssignTarget>,
    val vals : ArrayList<Expr>) : Statement()  {
}