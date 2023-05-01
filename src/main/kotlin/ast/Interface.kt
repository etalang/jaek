package ast

import edu.cornell.cs.cs4120.util.SExpPrinter

sealed class Interface(val headers: ArrayList<Definition>) : Eta() {

    class EtaInterface(headers : ArrayList<Definition>) : Interface(headers)

    class RhoInterface(val imports : MutableList<Use>, headers : ArrayList<Definition>) : Interface(headers)

    override fun write(printer: SExpPrinter) {
        printer.startList()
        printer.startList()
        headers.forEach { mh -> mh.write(printer) }
        printer.endList()
        printer.endList()
    }
}