package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Program(
    val imports: Imports,
    val definitions: Definitions
) : Node() {

    override fun write(printer: SExpPrinter) {
        printer.startList()
        imports.write(printer)
        definitions.write(printer)
        printer.endList()
    }
}

