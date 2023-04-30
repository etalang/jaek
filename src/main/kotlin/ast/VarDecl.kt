package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class VarDecl() : Statement() {

    class RawVarDeclList(val ids: ArrayList<String>, val type: Type, override val terminal: Terminal) : VarDecl() {

        override fun write(printer: SExpPrinter) {
//            printer.startList()
            ids.forEach { id ->
                printer.startList();
                printer.printAtom(id);
                type.write(printer);
                printer.endList()
            }
//
//            if (ids.size > 1) {
//                ids.forEach { id ->
//                    printer.startList();
//                    printer.printAtom(id);
//                    type.write(printer);
//                    printer.endList()
//                }
//            } else {
//                printer.startList()
//                printer.printAtom(ids[0])
//                type.write(printer)
//                printer.endList()
//            }
//
//
//            printer.printAtom(id)
//            type.write(printer)
//            printer.endList()
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
