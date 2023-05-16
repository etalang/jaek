package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Method(
    val id: String,
    val args: ArrayList<VarDecl.RawVarDeclList>,
    val returnTypes: ArrayList<Type>,
    override val terminal: Terminal
) : Definition() {
    var body: Statement.Block? = null

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(id)
        printList(printer, args)
        printList(printer, returnTypes)
        body?.write(printer)
        printer.endList()
    }
}