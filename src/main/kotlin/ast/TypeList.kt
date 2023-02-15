package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class TypeList : Node(){
    companion object {
        val typelist = mutableListOf<Type>()

        @JvmStatic fun prependType(t : Type) {
            typelist.add(0, t)
        }
    }
    override fun write(printer: SExpPrinter) {
        printer.startList()
        typelist.forEach {t -> t.write(printer)}
        printer.endList()
    }
}