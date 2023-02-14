package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class BinaryOp(val op : String, val left: Expr, val right: Expr) : Expr() {
    class Arith(op: String, left: Expr, right: Expr) : BinaryOp(op, left, right) {}

    class Compare(op: String, left: Expr, right: Expr) : BinaryOp(op, left, right) {}

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(this.op)
        printer.startList()
        this.left.write(printer)
        this.right.write(printer)
        printer.endList()
        printer.endList()
    }
}
