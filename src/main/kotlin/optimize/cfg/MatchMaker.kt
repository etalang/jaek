package optimize.cfg

class MatchMaker(val start: CFGNode, private val constructionMap: Map<String, CFGNode>) {
    /* node to nodes leading into it */
    private val predecessors: MutableMap<CFGNode, MutableSet<Pair<CFGNode, Boolean>>> = mutableMapOf()

    /* node to fall-throughs and jumps */
    private val successors: MutableMap<CFGNode, Successors> = mutableMapOf()

    fun allNodes(): Set<CFGNode> {
        return predecessors.keys union successors.keys
    }

    fun relevantNodes(): Set<CFGNode> {
        return predecessors.keys.plus(start)
    }

    fun fastNodesWithPredecessors(): Set<CFGNode> {
        return predecessors.keys
    }

    fun nodesWithJumpInto(): Set<CFGNode> {
        return predecessors.entries.filter { entry -> entry.value.filter { edgesIn -> edgesIn.second }.isNotEmpty() }.map { it.key }
            .toSet()
    }

    fun build(from: CFGNode, to: String, jump: Boolean) {
        constructionMap[to]?.let { connect(from, it, jump) }
    }

    fun connect(from: CFGNode, to: CFGNode, jump: Boolean) {
        successors.computeIfAbsent(from) { Successors() }.set(to, jump)
        predecessors.computeIfAbsent(to) { mutableSetOf() }.add(Pair(from, jump))
    }

    /** Given a -> b -> c. Removes b and connects a to c, preserving jump status of (a,b) */
    fun translate(a: CFGNode, b: CFGNode, c: CFGNode) {
        println("TRANSLATE")
        if (fallThrough(a) == b) {
            removeConnection(a, b, false)
            connect(a, c, false)
        } else if (jumpingTo(a) == b) {
            removeConnection(a, b, true)
            connect(a, c, true)
        }
    }

    fun removeNode(node: CFGNode) {
        //REMOVE CONNECTIONS IN
        val aboutToScrewWith = predecessors[node]?.toSet()
        aboutToScrewWith?.let {
            it.forEach { (pred, jump) ->
                removeConnection(pred, node, jump)
            }
        }
        //REMOVE CONNECTIONS OUT
        successors[node]?.let {
            it.jumpNode?.let { removeConnection(node, it, true) }
            it.fallThrough?.let { removeConnection(node, it, false) }
        }
        successors.remove(node)
    }

    fun removeConnection(from: CFGNode, to: CFGNode, jump: Boolean) {
        predecessors[to]?.remove(Pair(from, jump))
        if (predecessors[to]?.isEmpty() == true) predecessors.remove(to)
        successors[from]?.remove(to)
        if (successors[from]?.useless() == true) successors.remove(from)
    }

    fun fallThrough(node: CFGNode): CFGNode? {
        return successors[node]?.fallThrough
    }

    fun jumpingTo(node: CFGNode): CFGNode? {
        return successors[node]?.jumpNode
    }

    fun successors(node: CFGNode): Set<CFGNode> {
        return successors[node]?.successors() ?: emptySet()
    }

    fun successorEdges(node: CFGNode): Set<Edge> {
        return successors[node]?.successorEdges(node) ?: emptySet()
    }

    fun predecessors(node: CFGNode): Set<CFGNode> {
        return predecessors[node]?.map { it.first }?.toSet() ?: emptySet()
    }

    fun jumpsInto(node: CFGNode): Set<CFGNode> {
        return predecessors[node]?.filter { it.second }?.map { it.first }?.toSet() ?: emptySet()
    }

    fun fallThroughsInto(node: CFGNode): CFGNode? {
        return predecessors[node]?.filter { !it.second }?.map { it.first }?.firstOrNull()
    }

    fun predecessorEdges(node: CFGNode): Set<Edge> {
        return predecessors[node]?.map { Edge(it.first, node, it.second) }?.toSet() ?: emptySet()
    }

    fun removeAndLink(node: CFGNode): Boolean {
        val fallThrough = fallThrough(node)
        val jumpTo = jumpingTo(node)
        if (jumpTo == null && fallThrough == null) {
            removeNode(node)
            return true
        } else if (fallThrough != null && jumpTo == null) {
            predecessors(node).forEach { pred ->
                translate(pred, node, fallThrough)
            }
            return true
        } else if (jumpTo != null && fallThrough == null) {
            predecessors(node).forEach { pred ->
                translate(pred, node, jumpTo)
            }
            return true
        }
        return false
    }

    class Successors {
        var fallThrough: CFGNode? = null
        var jumpNode: CFGNode? = null
        fun set(node: CFGNode, jump: Boolean) {
            if (jump) {
                if (jumpNode != null) {
                    throw Exception("cannot override jump!")
                }
                jumpNode = node
            } else {
                if (fallThrough != null) {
                    throw Exception("cannot override fall through!")
                }
                fallThrough = node
            }
        }

        fun remove(node: CFGNode) {
            println()
            if (jumpNode == node) jumpNode = null
            if (fallThrough == node) fallThrough = null
        }

        fun successors(): Set<CFGNode> {
            return setOfNotNull(jumpNode, fallThrough)
        }

        fun successorEdges(node: CFGNode): Set<Edge> {
            val set = mutableSetOf<Edge>()
            jumpNode?.let { set.add(Edge(node, it, true)) }
            fallThrough?.let { set.add(Edge(node, it, false)) }
            return set
        }

        fun useless(): Boolean {
            return fallThrough == null && jumpNode == null
        }
    }
}