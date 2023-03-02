package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Statement : Node() {
    class If(val guard: Expr, val thenBlock: Statement, val elseBlock: Statement?, override val terminal: Terminal) :
        Statement() {

        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom("if")
            guard.write(printer)
            thenBlock.write(printer)
            elseBlock?.write(printer)
            printer.endList()
        }
    }

    class While(val guard: Expr, val body: Statement, override val terminal: Terminal) : Statement() {
        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom("while")
            guard.write(printer)
            body.write(printer)
            printer.endList()
        }
    }

    class Return(val args: List<Expr>, override val terminal: Terminal) : Statement() {
        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom("return")
            args.forEach { expr -> expr.write(printer) }
            printer.endList()
        }
    }

//    // probably could change typing to make this check better
//    sealed class DeclareInit {
//        class Declare(val decl : VarDecl) : DeclareInit()
//        class Init(val init: ArrayInit) : DeclareInit()
//    }
//    class DeclareInits(val declOrInit : List<DeclareInit>) : Statement()
    /* this is handled by MultiAssignBuilder, I think */
//    class Assignment(val id: Expr, val expr: Expr) : Statement() // changed this to be an expression, i.e. a[1] for instance
//
//    class DeclareAssign(val decl: VarDecl, val expr: Expr) : Statement() //not entirely sure if I love this
//    // changed the RHS to be an expression instead

    class Block(val stmts: List<Statement>, override val terminal: Terminal) : Statement() {

        override fun write(printer: SExpPrinter) {
            printer.startList()
            stmts.forEach { stmt -> stmt.write(printer) }
            printer.endList()
        }
    }

    class Procedure(val id: String, val args: List<Expr>, override val terminal: Terminal) : Statement() {

        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom(id)
            args.forEach { args -> args.write(printer) }
            printer.endList()
        }
    }

    class ArrayInit(val type: Type, initDim: Expr) : Statement() {
        /** dimensions gives the expressions in the initialization of the array in REVERSE order! */
        var dimensions: ArrayList<Expr?> = arrayListOf<Expr?>(initDim)
        override val terminal: Terminal = TODO("Not yet implemented")

        override fun write(printer: SExpPrinter) {
            dimensions.forEach {
                printer.startList()
                printer.printAtom("[]")
            }
            type.write(printer)
            dimensions.forEach { dim ->
                dim?.write(printer)
                printer.endList()
            }
        }
    }
}
