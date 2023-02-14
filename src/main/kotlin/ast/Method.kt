package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Method(
    val id: String,
    val args: ArrayList<Statement>,
    val returnTypes: ArrayList<Type>,
    val body: Statement.Block
) : Definition() {
    //TODO: not sure if should be Block or Statement
    override fun write(printer: SExpPrinter) { // this is redundant code, should
        printer.startList()
        printer.printAtom(id)
        printer.startList()
        args.forEach {stmt -> stmt.write(printer)}
        printer.endList()
        printer.startList()
        returnTypes.forEach {type -> type.write(printer)}
        printer.endList()
        body.write(printer)
        printer.endList()
    }
}