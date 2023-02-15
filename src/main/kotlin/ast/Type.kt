package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Type : Node() {
    data class Array(val t: Type) : Type()

    override fun write(printer: SExpPrinter) {
        when (this) {
            is Array -> {
                t.write(printer)
                printer.printAtom("[]")
            }
            else -> printer.printAtom("")

        }
    }
}