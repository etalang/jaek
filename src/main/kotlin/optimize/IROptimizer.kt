package optimize

import ir.lowered.LIRFuncDecl
import optimize.cfg.CFGBuilder
import java.io.File

class IROptimizer(val lir: LIRFuncDecl, optimize: Settings.Opt) {
    init {
        if (lir.name == "_Imain_paai") {
            val builder = CFGBuilder(lir)
            val cfg = builder.build()
            File("cfg.dot").writeText(cfg.graphViz())
        }
    }

}