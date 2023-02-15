package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class AssignTarget : Node() {
    class DeclAssign(val decl: Statement) : AssignTarget() // might be declaration in first spot?

    class ExprAssign(val target: Expr) : AssignTarget()

    class Underscore : AssignTarget()
    override fun write(printer: SExpPrinter) {
        when (this) {
            is DeclAssign -> decl.write(printer)

            is ExprAssign -> target.write(printer)

            is Underscore -> printer.printAtom("_")
        }
    }
}