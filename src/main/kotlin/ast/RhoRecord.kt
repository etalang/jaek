package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class RhoRecord(val name : String, val fields : List<VarDecl.RawVarDeclList>, override val terminal: Terminal) : Definition() {
    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.printAtom(name)
        printList(printer, fields)
        printer.endList()
    }
}

