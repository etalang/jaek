package optimize.dataflow

import optimize.cfg.CFG
import optimize.cfg.CFGNode
import optimize.cfg.Edge

class DeadCodeRem(cfg: CFG) : CFGFlow.Backward<DeadCodeRem.Info>(cfg), Properties.Use, Properties.Def, PostProc {
    private val mm = cfg.mm

    data class Info(val live : Set<String>) : EdgeValues() {
        override val pretty: String get() = live.toString()
    }

    override fun meet(e1: Info, e2: Info): Info {
//        val futureVars = e1.useMap.keys union e2.useMap.keys
//        val varSetMeet =
//            futureVars.map { e1.useMap.getOrDefault(it, emptySet()) union e1.useMap.getOrDefault(it, emptySet()) }
//        return Info(futureVars.zip(varSetMeet).toMap())
        return Info(e1.live union e2.live)
    }

    /** F_N (out) = use[n] ∪ (out - def[n]) */
    override fun transition(n: CFGNode, argumentInfo: Info): Map<Edge, Info> {
        val nodeUses = Info(use(n) union (argumentInfo.live - def(n)))
        return mm.predecessorEdges(n).associateWith { nodeUses }
    }

    override val top: Info = Info(emptySet<String>())
    override val name: String = "Dead Code Removal"
    override fun postprocess() {
        var deleteDead = true
        while (deleteDead) {
            deleteDead = deleteDeadAssigns()
            run()
        }
    }

    private fun deleteDeadAssigns(): Boolean {
        // if a variable is defined and is not live-out, delete the thing
        val remove = mm.fastNodesWithPredecessors().firstOrNull {
            it !is CFGNode.Start &&
                    (it is CFGNode.Gets && !bigMeet(mm.successorEdges(it)).live.contains(it.varName)) // TODO: consider adding func gets - would be hard
        }
        if (remove != null) {
            mm.removeAndLink(remove)
            return true
        }
        return false
    }


}