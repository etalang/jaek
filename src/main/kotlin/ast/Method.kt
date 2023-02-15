package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Method(
    val id: String,
    val args: ArrayList<VarDecl.RawVarDecl>,
    val returnTypes: ArrayList<Type>
) : Definition() {
    var body : Statement.Block? = null
        set(b) {
            field = b
        }

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(id)
        printList(printer, args)
        printList(printer, returnTypes)
        body?.write(printer)
        printer.endList()
    }
}