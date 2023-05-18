package optimize.dataflow

import optimize.cfg.CFG
import optimize.cfg.CFGNode
import optimize.cfg.Edge

class DeadCodeRem(cfg: CFG) : CFGFlow.Backward<DeadCodeRem.Info>(cfg), Properties.Use, Properties.Def, PostProc {
    private val mm = cfg.mm

    data class Info(val useMap: Map<CFGNode, Set<String>>) : EdgeValues() {
        override val pretty: String
            get() = "$useMap"

        fun copy(): Info {
            return Info(useMap.toMap())
        }

    }

    override fun meet(e1: Info, e2: Info): Info {
        val futureVars = e1.useMap.keys union e2.useMap.keys
        val varSetMeet =
            futureVars.map { e1.useMap.getOrDefault(it, emptySet()) union e1.useMap.getOrDefault(it, emptySet()) }
        return Info(futureVars.zip(varSetMeet).toMap())
    }

    override fun transition(n: CFGNode, inInfo: Info): Map<Edge, Info> {
        val nodeUses = use(n) union (inInfo.useMap.getOrDefault(n, emptySet()).minus(def(n)))
        return mm.predecessorEdges(n).associateWith { Info(inInfo.useMap + mapOf(n to nodeUses)) }
    }

    override val top: Info = Info(emptyMap())
    override val name: String = "Dead Code Removal"
    override fun postprocess() {
        var deleteDead = true
        while (deleteDead) {
            deleteDead = deleteDeadAssigns()
        }
    }

    private fun deleteDeadAssigns(): Boolean {
        // if a variable is defined and is not live-out, delete the thing
        val remove = mm.fastNodesWithPredecessors().firstOrNull {
            it !is CFGNode.Start &&
                    it is CFGNode.Gets && !bigMeet(mm.successorEdges(it)).useMap.getOrDefault(it, emptySet())
                .contains(it.varName) // TODO: consider adding func gets - would be hard
        }
        if (remove != null) {
            mm.removeNode(remove)
            return true
        }
        return false
    }


}