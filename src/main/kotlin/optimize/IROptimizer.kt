package optimize

import ir.lowered.LIRFuncDecl
import optimize.cfg.CFGBuilder
import java.io.File

class IROptimizer(val lir: LIRFuncDecl, optimize: Settings.Opt, outputCFG: Settings.OutputCFG) {
    init {
        if (outputCFG.initial != null) {
            val builder = CFGBuilder(lir)
            val cfg = builder.build()
            // construct the file_f_phase.dot name
            val funcFile = outputCFG.getOutInit(lir.name)
            funcFile?.writeText(cfg.graphViz())
        }
    }

}