package optimize

import ir.lowered.LIRFuncDecl
import optimize.cfg.CFGBuilder
import optimize.cfg.Dominating
import java.io.File

class IROptimizer(val lir: LIRFuncDecl, optimize: Settings.Opt, outputCFG: Settings.OutputCFG) {
    init {
        if (outputCFG.initial != null) {
            val builder = CFGBuilder(lir)
            val cfg = builder.build()
            // construct the file_f_phase.dot name
            val funcFile = outputCFG.getOutInit(lir.name)
            funcFile?.writeText(cfg.graphViz())

            val o = Dominating(cfg)
            o.run()
//            println(o.values)

            o.values.forEach { println("[${it.key.from.pretty} -> ${it.key.node.pretty}] : ${prettyMap(it.value)}") }
        }
    }

    private fun prettyMap(value: Dominating.Info): String {
        var x = "{"
        value.domMap.forEach {
            x += "${it.key.pretty}=${it.value.pretty}\n"
        }
        x+="}"
        return x
    }

}