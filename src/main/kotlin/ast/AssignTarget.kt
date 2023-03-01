package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class AssignTarget : Node() {
    class DeclAssign(val decl: VarDecl.RawVarDecl) : AssignTarget() // might be declaration in first spot?

//    class ExprAssign(val target: Expr) : AssignTarget()

    class ArrayAssign(val arrayAssign: Expr.ArrayAccess) : AssignTarget()

    class IdAssign(val idAssign: Expr.Identifier) : AssignTarget()

    class Underscore : AssignTarget()

    override fun write(printer: SExpPrinter) {
        when (this) {
            is DeclAssign -> decl.write(printer)

            is ArrayAssign -> arrayAssign.write(printer)

            is IdAssign -> idAssign.write(printer)

//            is ExprAssign -> target.write(printer)

            is Underscore -> printer.printAtom("_")
        }
    }
}