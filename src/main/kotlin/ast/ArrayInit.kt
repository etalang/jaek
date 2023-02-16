package ast

import edu.cornell.cs.cs4120.util.SExpPrinter
//
//sealed class ArrayInit : Statement() {
//     class ArrPtr(val p : Primitive, val baseDim : Expr) : ArrayInit()
//     class Dim(val baseInit : ArrayInit, val nextDim : Expr?) : ArrayInit()
//
//    override fun write(printer: SExpPrinter) {
//        when (this) {
//            is ArrPtr -> {
//                printer.startList()
//                printer.printAtom("[]")
//                p.write(printer)
//                baseDim.write(printer)
//                printer.endList()
//            }
//            is Dim -> {
//                printer.startList()
//                printer.printAtom("[]")
//                baseInit.write(printer)
//                nextDim?.write(printer)
//                printer.endList()
//            }
//        }
//    }
//}
