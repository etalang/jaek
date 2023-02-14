
package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

class Imports : Node() {
    companion object {
        val imports = mutableListOf<Use>()
        @JvmStatic fun prependImport(import : Use) {
            imports.add(0, import)
        }
    }

    override fun write(printer: SExpPrinter) {
        printer.startList()
        imports.forEach {u -> u.write(printer)}
        printer.endList()
    }
}