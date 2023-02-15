package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Program(
    val imports: MutableList<Use>,
    val definitions: MutableList<Definition>
) : Node() {

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printList(printer, imports)
        printList(printer, definitions)
        printer.endList()
    }
}

