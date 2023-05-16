package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Program(val imports: MutableList<Use>, val definitions: MutableList<Definition>) : Eta() {

    class EtaProgram(imports : MutableList<Use>, definitions : MutableList<Definition>) : Program(imports, definitions)

    class RhoModule(imports : MutableList<Use>, definitions: MutableList<Definition>) : Program(imports, definitions)

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printList(printer, imports)
        printList(printer, definitions)
        printer.endList()
    }
}

