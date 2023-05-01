package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Interface(val imports: MutableList<Use>, val headers: ArrayList<Definition>) : Eta() {
    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.startList()
        headers.forEach { mh -> mh.write(printer) }
        printer.endList()
        printer.endList()
    }
}