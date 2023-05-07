package optimize.dataflow.properties

import optimize.cfg.CFGNode

sealed interface Properties {
    interface Use : Properties {
        fun use(n: CFGNode): Set<String> {
            return when (n) {
                is CFGNode.Cricket -> TODO()
                is CFGNode.Funcking -> TODO()
                is CFGNode.If -> TODO()
                is CFGNode.Gets -> TODO()
                is CFGNode.Mem -> TODO()
                is CFGNode.Return -> TODO()
                is CFGNode.Start -> TODO()
            }
        }
    }
}