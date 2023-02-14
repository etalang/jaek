package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Method(
    val id: String,
    val args: ArrayList<Statement>,
    val returnTypes: ArrayList<Type>,
    val body: Statement.Block?
) : Definition() {
    //TODO: not sure if should be Block or Statement
    override fun write(printer: SExpPrinter) {
        TODO("Not yet implemented")
    }
}