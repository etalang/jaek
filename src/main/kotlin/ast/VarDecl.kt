package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class VarDecl() : Statement() {

    class RawVarDeclList(val ids: ArrayList<Expr.Identifier>, val type: Type, override val terminal: Terminal) : VarDecl() {
        override fun write(printer: SExpPrinter) {
            ids.forEach { id ->
                printer.startList();
                printer.printAtom(id.name);
                type.write(printer);
                printer.endList()
            }
        }
    }

    class InitArr(val id: String, val arrInit: ArrayInit, override val terminal: Terminal) : VarDecl() {

        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom(id)
            arrInit.write(printer)
            printer.endList()
        }
    }
}
