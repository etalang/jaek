package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class VarDecl(val id: String) : Statement() {

    class RawVarDecl(id: String, val type: Type, override val terminal: Terminal) : VarDecl(id) {

        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom(id)
            type.write(printer)
            printer.endList()
        }
    }

    class InitArr(id: String, val arrInit: ArrayInit, override val terminal: Terminal) : VarDecl(id) {

        override fun write(printer: SExpPrinter) {
            printer.startList()
            printer.printAtom(id)
            arrInit.write(printer)
            printer.endList()
        }
    }
}
