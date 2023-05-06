package optimize.cfg

import optimize.cfg.AnalysisElt.*

class LiveVar(val cfg : CFG) : CFGFlow() {
    // use maps as suggested by appel
    override val inMap : MutableMap<CFGNode, VarSet> = mutableMapOf()
    override val outMap : MutableMap<CFGNode, VarSet> = mutableMapOf()
    override val top = VarSet(setOf())
    override fun meet(e1: AnalysisElt, e2: AnalysisElt): AnalysisElt {
        TODO("Not yet implemented")
    }

//    fun meet(s1 : VarSet, s2 : VarSet) : VarSet {
//        return VarSet(s1.set union s2.set)
//    }

    override fun transition(n : CFGNode){
        // based on liveInMap @ node
    }
    fun getDefs(n: CFGNode) : Set<String>{ // consider: what if two analyses use defs - reuse with cascading analysis?
        return when (n) {
            is CFGNode.Cricket -> emptySet()
            is CFGNode.Funcking -> emptySet()
            is CFGNode.If -> emptySet()
            is CFGNode.Gets -> setOf(n.varName)
            is CFGNode.Mem -> emptySet()
            is CFGNode.Return -> emptySet()
            is CFGNode.Start -> emptySet()
        }
    }

    fun getUses(n: CFGNode) : Set<String>{
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