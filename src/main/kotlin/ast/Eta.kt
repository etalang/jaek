package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Eta : Node() {
    override val terminal: Terminal = Terminal(1, 1)

    override fun write(printer: SExpPrinter) {
        printer.printAtom("i love 4120 ta charles sherk")
    }
}