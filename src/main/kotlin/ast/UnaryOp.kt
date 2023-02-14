package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class UnaryOp(val arg: Expr) : Expr() {
    class IntNeg(arg: Expr) : UnaryOp(arg)
    class BoolNeg(arg: Expr) : UnaryOp(arg)

    override fun write(printer: SExpPrinter) {
        TODO("Not yet implemented")
    }
}

