package ast

class MultiAssignBuilder(
    val targets : MutableList<AssignTarget>,
    val vals : MutableList<Expr>) : Statement()  {
    fun toStatementList() :ArrayList<Statement> {
        return ArrayList();}
}