package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class MultiAssign(
    val targets: ArrayList<AssignTarget>,
    val vals: ArrayList<Expr>,
    override val terminal: Terminal
) : Statement() {

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom("=")
        if (targets.size > 1){
            printList(printer, targets)

        } else {
            targets.forEach{target -> target.write(printer) }
        }
        vals.forEach{value -> value.write(printer)}

        printer.endList()
    }
}