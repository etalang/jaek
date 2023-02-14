package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Node {
    abstract fun write(printer: SExpPrinter)
}