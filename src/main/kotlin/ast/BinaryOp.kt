package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class BinaryOp(val op: Operation, val left: Expr, val right: Expr) : Expr() {
    enum class Operation(val strVal : String) {
        PLUS("+"),
        MINUS("-"),
        TIMES("*"),
        HIGHTIMES("*>>"),
        DIVIDE("/"),
        MODULO("%"),
        LT("<"),
        LEQ("<="),
        GT(">"),
        GEQ(">="),
        EQB("=="),
        NEQB("!="),
        AND("&"),
        OR("|");
    }

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(op.strVal)
        this.left.write(printer)
        this.right.write(printer)
        printer.endList()
    }
}
