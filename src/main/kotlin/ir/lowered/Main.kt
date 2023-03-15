package ir.lowered

import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter
import java.io.PrintWriter

fun main() {
    var seq = LIRSeq(
        listOf(
            LIRStmt.LIRCJump(LIRExpr.LIRConst(0), LIRStmt.LIRLabel("L1"), LIRStmt.LIRLabel("L2")),
            LIRStmt.LIRLabel("L1"),
            LIRStmt.LIRCallStmt(LIRExpr.LIRConst(99), listOf()),
            LIRStmt.LIRJump(LIRExpr.LIRName("L2")),
            LIRStmt.LIRLabel("L2"),
            LIRStmt.LIRLabel("L3"),
            LIRStmt.LIRReturn(listOf())
        )
    )

    seq = LIRSeq(
        listOf(
            LIRStmt.LIRCJump(LIRExpr.LIRConst(0), LIRStmt.LIRLabel("L1"), LIRStmt.LIRLabel("L2")),
            LIRStmt.LIRLabel("L2"),
            LIRStmt.LIRCallStmt(LIRExpr.LIRConst(99), listOf()),
            LIRStmt.LIRLabel("L1"),
            LIRStmt.LIRReturn(listOf())
        )
    )
    val writer = CodeWriterSExpPrinter(PrintWriter(System.out))
    seq.java.printSExp(writer)
    writer.flush()
    println("AFTER")
    LIRSeq(seq.blockReordering()).java.printSExp(writer)
    writer.flush()
    writer.close()

}