package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Primitive : Type() {
    object INT : Primitive()
    object BOOL : Primitive()

    override fun write(printer: SExpPrinter) {
        when (this) {
            is INT -> printer.printAtom("int")
            is BOOL -> printer.printAtom("bool")
        }
    }
}