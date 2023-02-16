package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Definition : Node() { //
    override fun write(printer: SExpPrinter) {
        when (this) {
            is Method -> this.write(printer)
            is GlobalDecl -> this.write(printer)
        }
    }
}
