package ast

import edu.cornell.cs.cs4120.util.SExpPrinter
import javax.lang.model.type.PrimitiveType

sealed class Type : Node() {
    object Int : Type()
    object Bool : Type()
    data class Array(val t: Type) : Type() //sorta see where you're going with this?

    override fun write(printer: SExpPrinter) {
        when (this) {
            is Array -> {
                t.write(printer)
                printer.printAtom("[]")
            }
            is Int -> printer.printAtom("int")
            is Bool -> printer.printAtom("bool")
        }
    }
}