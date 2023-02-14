package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class GlobalDefn(val decl: Declaration, val value: Literal?) : Definition() {
    override fun write(printer: SExpPrinter) {
        TODO("Not yet implemented")
    }
}