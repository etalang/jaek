package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Definition() : Node() {
    class Method(
        val name: Expr,
        val args: ArrayList<Statement>,
        val returnTypes: ArrayList<Type>,
        val body: Statement.Block?
    ) :
        Definition() {
        override fun write(printer: SExpPrinter) {
            TODO("Not yet implemented")
        }
    }

}
