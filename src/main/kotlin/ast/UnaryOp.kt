package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class UnaryOp(val op: Operation, val arg: Expr) : Expr() {

    enum class Operation(val strVal : String) {
        NOT("!"),
        NEG("-")
    }

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(op.strVal)
        this.arg.write(printer)
        printer.endList()
    }
}

