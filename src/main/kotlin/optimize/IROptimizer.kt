package optimize

import Settings
import ir.lowered.LIRFuncDecl

class IROptimizer(val lir: LIRFuncDecl, optimize: Settings.Opt, outputCFG: Settings.OutputCFG) {
    interface Graphable {
        fun graphViz(): String
    }
}