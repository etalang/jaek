package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Statement : Node() {
    class If(val guard: Expr, val thenBlock: Statement, val elseBlock: Statement?) : Statement()

    class While(val guard: Expr, val body: Statement) : Statement()

    class Return(val args: ArrayList<Expr>) : Statement()

    class Assignment(val id: String, val expr: Expr) : Statement()

    class DeclareAssign(val decl: Declaration, val assign: Assignment) : Statement() //not entirely sure if I love this

    class Block(val stmts: ArrayList<Statement>) : Statement()

    class CallProc(val id: Expr, val args: ArrayList<Expr>) : Statement()

    override fun write(printer: SExpPrinter) {
        TODO("Not yet implemented")
    }

}
