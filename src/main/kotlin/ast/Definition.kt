package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Definition : Node() {
    class Method(
        val name: Expr,
        val args: ArrayList<Statement>, // maybe a declaration here instead?
        val returnTypes: ArrayList<Type>,
        val body: Statement.Block?
    ) :
        Definition() {
        override fun write(printer: SExpPrinter) {
            printer.startList()
            name.write(printer)
            printer.startList()
            args.forEach {stmt -> stmt.write(printer)}
            printer.endList()
            printer.startList()
            returnTypes.forEach {type -> type.write(printer)}
            printer.endList()
            body?.write(printer)
            printer.endList()
        }
    }

}
