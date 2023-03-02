package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class GlobalDecl(val id: String, val type: Type, val value: Literal?, override val terminal: Terminal) : Definition() {

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(":global")
        printer.printAtom(id)
        type.write(printer)
        value?.write(printer)
        printer.endList()
    }
}