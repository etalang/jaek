package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Method(
    val methodHeader : MethodInterface,
    val body: Statement.Block?
) : Definition() {
    override fun write(printer: SExpPrinter) {
        printer.startList()
        methodHeader.write(printer)
        body?.write(printer)
        printer.endList()
    }
}