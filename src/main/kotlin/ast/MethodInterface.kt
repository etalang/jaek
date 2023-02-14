package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class MethodInterface(
    val id: String,
    val args: ArrayList<Statement>,
    val returnTypes: ArrayList<Type>) : Node() {

    override fun write(printer: SExpPrinter) {
        // TODO: Very similar code to Method, could be refactored?
        printer.startList()
        printer.printAtom(id)
        printer.startList()
        args.forEach {stmt -> stmt.write(printer)}
        printer.endList()
        printer.startList()
        returnTypes.forEach {type -> type.write(printer)}
        printer.endList()
        printer.endList()
    }
}