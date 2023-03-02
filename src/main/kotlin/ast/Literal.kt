package ast

import LexUtil
import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Literal : Expr() {
    class IntLit(val num: Long, override val terminal: Terminal) : Literal() {

        override fun write(printer: SExpPrinter) {
            if (num == Long.MIN_VALUE) {
                printer.startList()
                printer.printAtom("- 9223372036854775808")
                printer.endList()
            } else if (num < 0) {
                printer.startList()
                printer.printAtom("-")
                printer.printAtom((-1 * num).toString())
                printer.endList()
            } else {
                printer.printAtom(num.toString())
            }
        }
    }

    class StringLit(val text: String, override val terminal: Terminal) : Literal() {

        override fun write(printer: SExpPrinter) {
            printer.printAtom("\"${text}\"")
        }
    }

    class BoolLit(val bool: Boolean, override val terminal: Terminal) : Literal() {

        override fun write(printer: SExpPrinter) {
            printer.printAtom(bool.toString())
        }
    }

    class CharLit(val char: Int, override val terminal: Terminal) : Literal() {

        override fun write(printer: SExpPrinter) {
            printer.printAtom("'${LexUtil.formatChar(char)}'")
        }
    }

    class ArrayLit(val list: ArrayList<Expr>, override val terminal: Terminal) : Literal() {

        override fun write(printer: SExpPrinter) {
            printer.startList()
            list.forEach { e -> e.write(printer) }
            printer.endList()
        }
    }

}
