package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class MethodInterface(
    val id: String,
    val args: Declarations,
    val returnTypes: TypeList) : Node() {

    override fun write(printer: SExpPrinter) {
        // there is no list around this! this is intentional!
        printer.printAtom(id)
        args.write(printer)
        returnTypes.write(printer)
    }
}