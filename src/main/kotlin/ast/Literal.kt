package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Literal : Node() {
    class IntLit(val num: Long) : Literal()

    class StringLit(val text: String) : Literal()

    class BoolLit(val bool: Boolean) : Literal()

    class ArrayLit(val list: ArrayList<Expr>) : Literal()

    override fun write(printer: SExpPrinter) {
        TODO("Not yet implemented")
    }
}
