package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Eta : Node() {
    override val terminal: Terminal = Terminal(1, 1)

    override fun write(printer: SExpPrinter) {
        printer.printAtom("https://www.reddit.com/r/Cornell/comments/j70l1g/4820_auto_grader_output/")
    }
}