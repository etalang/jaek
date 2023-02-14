package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class UnaryOp(val arg: Expr) : Expr() {
    class IntNeg(arg: Expr) : UnaryOp(arg)
    class BoolNeg(arg: Expr) : UnaryOp(arg)

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(
            when (this) {
                is IntNeg -> "-"
                is BoolNeg -> "!"
            })
        printer.startList()
        this.arg.write(printer)
        printer.endList()
        printer.endList()
    }
}

