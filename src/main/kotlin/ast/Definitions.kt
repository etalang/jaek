package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Definitions : Node() {
    companion object {
        val defns = mutableListOf<Definition>()
        @JvmStatic fun prependDefn(defn : Definition) {
            defns.add(0, defn)
        }
    }

    override fun write(printer: SExpPrinter) {
        printer.startList()
        defns.forEach { d -> d.write(printer)}
        printer.endList()
    }
}