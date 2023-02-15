package ast

class MultiAssignBuilder(
    val decls : MutableList<GlobalDecl>,
    val vals : MutableList<Literal>?) : Statement()  {
    fun toStatementList() :ArrayList<Statement> {
        return ArrayList();}
}