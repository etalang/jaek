package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Expr : Node() {
    class FunctionCall(val fn: String, val args: ArrayList<Expr>, override val terminal: Terminal) : Expr() {

        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom(fn)
            args.forEach { expr -> expr.write(printer) }
            printer.endList()
        }

        class LengthFn(val arg: Expr, override val terminal: Terminal) : Expr() {

            override fun write(printer: SExpPrinter) {
                printer.startList()
                printer.printAtom("length")
                arg.write(printer)
                printer.endList()
            }

        }
    }

    class ArrayAccess(val arr: Expr, val idx: Expr) : Expr() {
        override val terminal: Terminal = TODO("Not yet implemented")

        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom("[]")
            arr.write(printer)
            idx.write(printer)
            printer.endList()
        }
    }

    class Identifier(val name: String, override val terminal: Terminal) : Expr() {

        override fun write(printer: SExpPrinter) {
            printer.printAtom(name)
        }

    }


}
