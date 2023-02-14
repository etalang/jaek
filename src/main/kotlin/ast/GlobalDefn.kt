package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class GlobalDefn(val decl: Declaration, val value: Literal?) : Definition() {
    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom("global:")
        decl.write(printer)
        value?.write(printer)
        printer.endList()
    }
}