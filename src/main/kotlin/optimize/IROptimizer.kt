package optimize

import ir.lowered.LIRFuncDecl
import optimize.cfg.CFGBuilder
import optimize.cfg.CFGNode
import java.io.File

class IROptimizer(val lir: LIRFuncDecl) {
    init {
        if (lir.name == "_Imain_paai") {
            val builder = CFGBuilder(lir)
            val cfg = builder.build()
            File("cfg.dot").writeText(cfg.graphViz())
        }
    }

}