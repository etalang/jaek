package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Primitive(override val terminal: Terminal) : Type() {
    class INT(terminal: Terminal) : Primitive(terminal)
    class BOOL(terminal: Terminal) : Primitive(terminal)

    override fun write(printer: SExpPrinter) {
        when (this) {
            is INT -> printer.printAtom("int")
            is BOOL -> printer.printAtom("bool")
        }
    }
}