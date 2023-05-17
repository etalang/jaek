package optimize

import Settings
import ir.lowered.LIRFuncDecl
import optimize.cfg.CFG
import optimize.cfg.CFGBuilder
import optimize.cfg.CFGDestroyer
import optimize.dataflow.CondConstProp
import optimize.dataflow.CopyProp
import optimize.dataflow.DeadCodeRem

class IROptimizer(val lir: LIRFuncDecl, optimize: Settings.Opt, val outputCFG: Settings.OutputCFG) {
    var cfg: CFG

    init {
        val builder = CFGBuilder(lir)
        cfg = builder.build()
        val funcFile = outputCFG.getOutFile(lir.name, "initial")
        funcFile?.writeText(cfg.graphViz())

        for (i in 0 until 5) {
            if (optimize.desire(Settings.Opt.Actions.cp)) {
                val ccp = CondConstProp(cfg)
                ccp.run()
                ccp.postprocess()
            }

//            if (optimize.desire(Settings.Opt.Actions.dce)) {
//                val dce = DeadCodeRem(cfg)
//                dce.run()
//                dce.postprocess()
//            }

            if (optimize.desire(Settings.Opt.Actions.copy)) {
                val copypop = CopyProp(cfg)
                copypop.run()
                copypop.postprocess()
            }

            val newLIR = CFGDestroyer(cfg, lir).destroy()
            newLIR.reorderBlocks()
            cfg = CFGBuilder(newLIR).build()
        }

        val postFile = outputCFG.getOutFile(lir.name, "final")
        postFile?.writeText(cfg.graphViz())
    }

    fun destroy(): LIRFuncDecl {
        return CFGDestroyer(cfg, lir).destroy()
    }

    interface Graphable {
        fun graphViz(): String
    }
}