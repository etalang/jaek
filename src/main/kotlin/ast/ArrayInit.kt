package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class ArrayInit : Statement() {
    data class ArrPtr(val p : Primitive, val baseDim : Expr) : ArrayInit()
    data class Dim(val baseInit : ArrayInit, val nextDim : Expr?) : ArrayInit()

    override fun write(printer: SExpPrinter) {
        when (this) {
            is ArrPtr -> {
                p.write(printer)
                printer.printAtom("[")
                printer.printAtom(baseDim.toString())
                printer.printAtom("]")
            }
            is Dim -> {
                baseInit.write(printer)
                printer.printAtom("[")
                if (nextDim != null) {
                    printer.printAtom(nextDim.toString())
                }
                printer.printAtom("]")
            }
        }
    }
}
