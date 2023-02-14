package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Program(
    val imports: ArrayList<Use>,
    val definitions: ArrayList<Definition>
) : Node() {

    override fun write(printer: SExpPrinter) {
        printer.startList()
        imports.forEach { use -> use.write(printer) }
        definitions.forEach { defn -> defn.write(printer) }
        printer.endList()
    }
}

