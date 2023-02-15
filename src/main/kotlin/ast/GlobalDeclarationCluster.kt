package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

@Deprecated("what")
class GlobalDeclarationCluster(
    val decls : MutableList<GlobalDecl>,
    val vals : MutableList<Literal>?) : Definition() {
    override fun write(printer: SExpPrinter) {
        if (vals != null) {
            printer.startList()
            printer.printAtom("=")
            printList(printer, decls)
            printList(printer, vals)
            printer.endList()
        }
        else {
            printList(printer, decls)
        }
    }
}