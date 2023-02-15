package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Type : Node() {
    class Array(val t: Type) : Type() {
        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom("[]")
            t.write(printer)
            printer.endList()
        }
    }

}