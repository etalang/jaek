package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class ArrayInit : Node() {
    data class ArrPtr(val p : Primitive, val baseDim : Int) : ArrayInit()
    data class Dim(val baseInit : ArrayInit, val nextDim : Int) : ArrayInit()

    override fun write(printer: SExpPrinter) {
        when (this) {
            is ArrPtr -> {
                p.write(printer)
                printer.printAtom("[")
                assert(baseDim != 0)
                printer.printAtom(baseDim.toString())
                printer.printAtom("]")
            }
            is Dim -> {
                baseInit.write(printer)
                printer.printAtom("[")
                if (nextDim != 0) {
                    printer.printAtom(nextDim.toString())
                }
                printer.printAtom("]")
            }
        }
    }
}
