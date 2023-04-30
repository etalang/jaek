package optimize

import ir.lowered.LIRCompUnit
import ir.lowered.LIRFuncDecl
import optimize.cfg.CFGBuilder
import optimize.cfg.CFGNode
import java.io.File

class IROptimizer(val lir : LIRFuncDecl) {
    init {
        val builder = CFGBuilder(lir)
        var cfg : CFGNode = builder.start
        File("cfg.dot").writeText(builder.graphViz())
    }

}