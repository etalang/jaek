package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class BinaryOp(val left: Expr, val right: Expr) : Expr() {
    class Arith(val op: String, left: Expr, right: Expr) : BinaryOp(left, right) {}

    class Compare(val op: String, left: Expr, right: Expr) : BinaryOp(left, right) {}

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(
            when (this) {
                //this is the cool stuff
                is Arith -> ""
                is Compare -> ""
            }
        )
        printer.endList()
    }
}
