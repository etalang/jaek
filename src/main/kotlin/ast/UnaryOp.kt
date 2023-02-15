package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class UnaryOp(val op: Operation, val arg: Expr) : Expr() {

    enum class Operation(val strVal : String) {
        NOT("!"),
        NEG("-")
    }
//    class IntNeg(arg: Expr) : UnaryOp(arg)
//    class BoolNeg(arg: Expr) : UnaryOp(arg)

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(op.strVal)
        printer.startList()
        this.arg.write(printer)
        printer.endList()
        printer.endList()
    }
}

