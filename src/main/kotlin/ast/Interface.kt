package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Interface(val imports: ArrayList<Use>, val methodHeaders : ArrayList<Method>) : Eta() {
    override fun write(printer: SExpPrinter) {
        printer.startList()
        if (imports.size > 0) {
            printer.startList()
            imports.forEach { use -> use.write(printer) }
            printer.endList()
        }
        printer.startList()
        methodHeaders.forEach {mh -> mh.write(printer)}
        printer.endList()
        printer.endList()
    }
}