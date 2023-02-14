package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Statement : Node() {
    class If(val guard: Expr, val thenBlock: Statement, val elseBlock: Statement?) : Statement()

    class While(val guard: Expr, val body: Statement) : Statement()

    class Return(val args: ArrayList<Expr>) : Statement()

    class Assignment(val id: Expr, val expr: Expr) : Statement() // changed this to be an expression, i.e. a[1] for instance

    class DeclareAssign(val decl: Declaration, val expr: Expr) : Statement() //not entirely sure if I love this
    // changed the RHS to be an expression instead

    class Block(val stmts: ArrayList<Statement>) : Statement()

    class CallProc(val id: Expr, val args: ArrayList<Expr>) : Statement()

    override fun write(printer: SExpPrinter) {
        printer.startList()
        when (this) {
            is If -> {
                printer.printAtom("if")
                printer.startList()
                guard.write(printer)
                printer.endList()
                printer.startList()
                thenBlock.write(printer)
                printer.endList()
                printer.startList()
                elseBlock?.write(printer)
                printer.endList()
            }
            is While -> {
                printer.printAtom("while")
                printer.startList()
                guard.write(printer)
                printer.endList()
                printer.startList()
                body.write(printer)
                printer.endList()
            }
            is Return -> {
                printer.printAtom("return")
                args.forEach {expr -> expr.write(printer)}
            }
            is Assignment -> {
                printer.printAtom("=")
                printer.startList()
                id.write(printer)
                printer.endList()
                printer.startList()
                expr.write(printer)
                printer.endList()
            }
            is DeclareAssign -> {
                printer.printAtom("=")
                printer.startList()
                decl.write(printer)
                printer.endList()
                printer.startList()
                expr.write(printer)
                printer.endList()
            }
            is Block -> {
                printer.startList()
                stmts.forEach { stmt -> stmt.write(printer) }
                printer.endList()
            }
            is CallProc -> {
                printer.startList()
                id.write(printer)
                printer.startList()
                args.forEach {expr -> expr.write(printer)}
                printer.endList()
                printer.endList()
            }
            else -> {
                printer.printAtom("")
            }
        }
        printer.endList()
    }

}
