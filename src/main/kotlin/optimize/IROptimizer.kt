package optimize

import Settings
import ir.lowered.LIRFuncDecl
import optimize.cfg.CFGBuilder
import optimize.dataflow.Dominating
import java.io.File

class IROptimizer(val lir: LIRFuncDecl, optimize: Settings.Opt, outputCFG: Settings.OutputCFG) {
    init {
        if (outputCFG.initial != null) {
            val builder = CFGBuilder(lir)
            val cfg = builder.build()
            // construct the file_f_phase.dot name
            val funcFile = outputCFG.getOutInit(lir.name)
            funcFile?.writeText(cfg.graphViz())

            if (lir.name == "_Imain_paai") {
                val o = Dominating(cfg)
                o.run()
                o.values.forEach { println("[〚${it.key.from.pretty}〛 -> 〚${it.key.node.pretty}〛] : ${it.value.doms}") }
                File("maindataflow'.dot").writeText(o.graphViz())
            }
        }
    }

    interface Graphable {
        fun graphViz(): String
    }
}