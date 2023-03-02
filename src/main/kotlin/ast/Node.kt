package ast

import edu.cornell.cs.cs4120.util.SExpPrinter
import typechecker.EtaType

sealed class Node {
    var etaType: EtaType? = null
    abstract val terminal: Terminal

    abstract fun write(printer: SExpPrinter)
    fun printList(printer: SExpPrinter, lst: List<Node>) {
        printer.startList()
        lst.forEach { n -> n.write(printer) }
        printer.endList()
    }
}