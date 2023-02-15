package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class ArrayInit : Statement() {
     class ArrPtr(val p : Primitive, val baseDim : Expr) : ArrayInit()
     class Dim(val baseInit : ArrayInit, val nextDim : Expr?) : ArrayInit()

    override fun write(printer: SExpPrinter) {
        when (this) {
            is ArrPtr -> {
                p.write(printer)
                printer.printAtom("[")
                baseDim.write(printer)
                printer.printAtom("]")
            }
            is Dim -> {
                baseInit.write(printer)
                printer.printAtom("[")
                nextDim?.write(printer)
                printer.printAtom("]")
            }
        }
    }
}
