package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Eta : Node() {

    override fun write(printer: SExpPrinter) {
        printer.printAtom("i love 4120 ta charles sherk")
    }
}