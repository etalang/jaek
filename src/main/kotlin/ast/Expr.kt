package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Expr : Node() {
    //TODO finish
    class FnCall(val fn: Expr, val args: ArrayList<Expr>) : Expr()

    class ArrayIdx(val arr: Expr, val idx: Expr): Expr()

    data class Identifier(val name : String): Expr()

    override fun write(printer: SExpPrinter) {
        printer.startList()
        when (this) {
            is FnCall -> {
                fn.write(printer)
                printer.startList()
                args.forEach {expr -> expr.write(printer)}
                printer.endList()
            }
            is ArrayIdx -> {
                printer.printAtom("[]")
                arr.write(printer)
                idx.write(printer)
            }
            is Identifier -> {
                printer.printAtom(name)
            }
            else -> printer.printAtom("")
        }

    }

}
