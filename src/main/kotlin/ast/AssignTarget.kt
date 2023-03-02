package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class AssignTarget : Node() {
    class DeclAssign(val decl: VarDecl.RawVarDecl) : AssignTarget() {
        // might be declaration in first spot?
        override val terminal: Terminal = decl.terminal
    }

    class ArrayAssign(val arrayAssign: Expr.ArrayAccess) : AssignTarget() {
        override val terminal: Terminal = arrayAssign.terminal
    }

    class IdAssign(val idAssign: Expr.Identifier) : AssignTarget() {
        override val terminal: Terminal = idAssign.terminal
    }

    class Underscore(override val terminal: Terminal) : AssignTarget()

    override fun write(printer: SExpPrinter) {
        when (this) {
            is DeclAssign -> decl.write(printer)

            is ArrayAssign -> arrayAssign.write(printer)

            is IdAssign -> idAssign.write(printer)

            is Underscore -> printer.printAtom("_")
        }
    }
}