package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Declarations : Node() { // might be able to write a constructor that handles this more smoothly?
    companion object {
        val decls = mutableListOf<VarDecl>()

        @JvmStatic fun prependDecl(decl : VarDecl) {
            decls.add(0, decl)
        }
    }
    override fun write(printer: SExpPrinter) {
        printer.startList()
        decls.forEach {decl -> decl.write(printer)}
        printer.endList()
    }
}