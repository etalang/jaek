package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Use(val lib: String, override val terminal: Terminal) : Node() {

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom("use")
        printer.printAtom(lib)
        printer.endList()
    }
}

