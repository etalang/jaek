package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class MultiAssign(
    val targets: ArrayList<AssignTarget>,
    val vals: ArrayList<Expr>
) : Statement() {

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom("=")
        if (targets.size > 1){
            printer.startList()
            printList(printer, targets)
            printer.endList()
            printer.startList()
            printList(printer, vals)
            printer.endList()
        } else {
            printList(printer, targets)
            printList(printer, vals)
        }
        printer.endList()
    }
}