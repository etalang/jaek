package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Literal : Expr() {
    class IntLit(val num: Long) : Literal()

    class StringLit(val text: String) : Literal()

    class BoolLit(val bool: Boolean) : Literal()

    class CharLit(val char: Char) : Literal()

    class ArrayLit(val list: ArrayList<Expr>) : Literal()

    override fun write(printer: SExpPrinter) {
        when (this) {
            // this could use a refactor
            is BoolLit -> printer.printAtom(bool.toString())
            is IntLit -> printer.printAtom(num.toString())
            is StringLit -> printer.printAtom(text)
            is CharLit -> printer.printAtom(char.toString())
            is ArrayLit -> {
                printer.startList()
                list.forEach { e -> e.write(printer) }
                printer.endList()
            }
        }

    }
}
