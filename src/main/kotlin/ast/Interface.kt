package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Interface(val methodHeaders : ArrayList<MethodInterface>) : Node () {
    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.startList()
        methodHeaders.forEach {mh -> mh.write(printer)}
        printer.endList()
        printer.endList()
    }
}