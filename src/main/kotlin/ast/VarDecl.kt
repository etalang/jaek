package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class VarDecl(val id: String, val type: Type) : Statement() {
    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(id)
        printer.printAtom(type.toString())
        printer.endList()
    }
}
