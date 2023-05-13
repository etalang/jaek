package optimize

import Settings
import ir.lowered.LIRFuncDecl
import optimize.cfg.CFG
import optimize.cfg.CFGBuilder
import optimize.cfg.CFGDestroyer
import optimize.dataflow.CondConstProp
import optimize.dataflow.Dominating
import java.io.File

class IROptimizer(val lir: LIRFuncDecl, optimize: Settings.Opt, outputCFG: Settings.OutputCFG) {
    val cfg : CFG
    init {
        val builder = CFGBuilder(lir)
        cfg = builder.build()
        // construct the file_f_phase.dot name
        val funcFile = outputCFG.getOutInit(lir.name)
        funcFile?.writeText(cfg.graphViz())
        val o = CondConstProp(cfg)
        o.run()
        if (lir.name == "_Imain_paai") File("preproc.dot").writeText(o.graphViz())
        o.postprocess()
        if (lir.name == "_Imain_paai") File("postproc.dot").writeText(o.graphViz())


        if (lir.name == "_Imain_paai") {
//                val o = Dominating(cfg)

//                o.values.forEach { println("[〚${it.key.from.pretty}〛 -> 〚${it.key.node.pretty}〛] : ${it.value.doms}") }
            o.values.forEach { println("[〚${it.key.from.pretty}〛 -> 〚${it.key.node.pretty}〛] : ${it.value.pretty}") }
            File("maindataflowpreproc.dot").writeText(o.graphViz())

            File("maindataflowpostproc.dot").writeText(o.graphViz())
        }

    }

    fun destroy() : LIRFuncDecl {
        return CFGDestroyer(cfg, lir).destroy()
    }

    interface Graphable {
        fun graphViz(): String
    }
}