package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Literal : Expr() {
    class IntLit(val num: Long) : Literal() {
        override fun write(printer: SExpPrinter) {
            printer.printAtom(num.toString());
        }
    }

    class StringLit(val text: String) : Literal() {
        override fun write(printer: SExpPrinter) {
            printer.printAtom(text)
        }
    }

    class BoolLit(val bool: Boolean) : Literal() {
        override fun write(printer: SExpPrinter) {
            printer.printAtom(bool.toString())
        }
    }

    class CharLit(val char: Int) : Literal() {
        override fun write(printer: SExpPrinter) {
            printer.printAtom(char.toString())
        }
    }

    class ArrayLit(val list: ArrayList<Expr>) : Literal() {
        override fun write(printer: SExpPrinter) {
            printer.startList()
            list.forEach { e -> e.write(printer) }
            printer.endList()
        }
    }

}
