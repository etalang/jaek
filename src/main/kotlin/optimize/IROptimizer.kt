package optimize

import Settings
import ir.lowered.LIRFuncDecl
import optimize.cfg.CFG
import optimize.cfg.CFGBuilder
import optimize.cfg.CFGDestroyer
import optimize.dataflow.CondConstProp
import optimize.dataflow.DeadCodeRem
import optimize.dataflow.Dominating
import java.io.File

class IROptimizer(val lir: LIRFuncDecl, optimize: Settings.Opt, outputCFG: Settings.OutputCFG) {
    val cfg: CFG

    init {
        val builder = CFGBuilder(lir)
        cfg = builder.build()
        // construct the file_f_phase.dot name
        val funcFile = outputCFG.getOutFile(lir.name, "initial")
        funcFile?.writeText(cfg.graphViz())
        Dominating(cfg).run()
//        val o = DeadCodeRem(cfg)
        val ccp = CondConstProp(cfg)
        ccp.run()
        ccp.postprocess()
//        val o = DeadCodeRem(cfg)
//        o.run()
//        if (lir.name.contains("f")) File("shit/preproc.dot").writeText(o.graphViz())
//        o.postprocess()
//        if (lir.name == "_Imain_paai") File("postproc.dot").writeText(o.graphViz())
        val postFile = outputCFG.getOutFile(lir.name, "final")
        postFile?.writeText(cfg.graphViz())
//                    File("maindataflowpostproc.dot").writeText(o.graphViz())


        if (lir.name == "_Imain_paai") {
//                val o = Dominating(cfg)

//        if (lir.name.contains("f")) File("shit/postproc.dot").writeText(o.graphViz())
//        if (lir.name == "_Imain_paai") {
//        val o = Dominating(cfg)
//        o.run()
//            println("SDKJHSDFKJHFSDKJHDFSHJK")
//                o.values.forEach { println("[〚${it.key.from.pretty}〛 -> 〚${it.key.node.pretty}〛] : ${it.value.useMap}") }
//            o.values.forEach { println("[〚${it.key.from.pretty}〛 -> 〚${it.key.node.pretty}〛] : ${it.value.pretty}") }
//        File("maindataflowpreproc.dot").writeText(o.graphViz())

//            File("maindataflowpostproc.dot").writeText(o.graphViz())
        }

    }

    fun destroy(): LIRFuncDecl {
        return CFGDestroyer(cfg, lir).destroy()
    }

    interface Graphable {
        fun graphViz(): String
    }
}