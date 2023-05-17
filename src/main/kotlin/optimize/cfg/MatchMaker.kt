package optimize.cfg

class MatchMaker(val start: CFGNode, private val constructionMap: Map<String, CFGNode>) {
    /* node to nodes leading into it */
    private val predecessors: MutableMap<CFGNode, MutableSet<Pair<CFGNode, Boolean>>> = mutableMapOf()

    /* node to fall-throughs and jumps */
    private val successors: MutableMap<CFGNode, Successors> = mutableMapOf()

    fun repOp() {
        allNodes().forEach {
            predecessors[it]?.forEach { (pred, jump) ->
                if (jump) require(jumpingTo(pred) == it)
                else require(fallThrough(pred) == it)
            }
            require((predecessors[it]?.filter { !it.second }?.size ?: 0) < 2) //fall-throughs In
            successors[it]?.let { l ->
                l.fallThrough?.let { succ -> require(predecessors[succ]?.contains(Pair(it, false)) ?: false) }
                l.jumpNode?.let { succ -> require(predecessors[succ]?.contains(Pair(it, true)) ?: false) }
            }
        }
    }

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
        return predecessors.entries.filter { entry -> entry.value.any { edgesIn -> edgesIn.second } }.map { it.key }
            .toSet()
    }

    fun nodesWithNoFallThroughsMinusStart(): List<CFGNode> {
        return predecessors.entries.filter { entry ->
            entry.key !is CFGNode.Start && entry.value.none { edgesIn -> !edgesIn.second }
        }.map { it.key }
    }

    fun build(from: CFGNode, to: String, jump: Boolean) {
        constructionMap[to]?.let { connect(from, it, jump) }
    }

    fun connect(from: CFGNode, to: CFGNode, jump: Boolean) {
        repOp()
        if (!jump && predecessors[to]?.any { !it.second } == true) {
            val dummy = CFGNode.NOOP()
            connect(from, dummy, false)
            connect(dummy, to, true)
            println("SHIT'S FUNKY")
            return
        } else {
            successors.computeIfAbsent(from) { Successors() }.set(to, jump)
            predecessors.computeIfAbsent(to) { mutableSetOf() }.let {
                require(jump || !it.any { !it.second })
                it.add(Pair(from, jump))
            }
        }
        repOp()
    }

    /**
     * Given a -> b -> c. Removes b and connects a to c.
     *
     * Jxy = jump status (x,y) (X, Y) = ( jump status(a,b) , jump status (b,c) )
     * - (JUMP, JUMP) => JUMP
     * - (JUMP, FALL) => JUMP
     * - (FALL, JUMP) => FALL -> DUMMY JUMP
     * - (FALL, FALL) => FALL
     */
    fun translate(a: CFGNode, b: CFGNode, c: CFGNode) {
        require(fallThrough(a) == b || jumpingTo(a) == b)
        val jumpAB = jumpingTo(a) == b
        val jumpBC = jumpingTo(b) == c

        if (jumpAB) {
            //JUMP-JUMP or JUMP-FALL
            removeConnection(a, b, true)
            removeConnection(b, c, jumpBC)
            connect(a, c, true)
        } else if (jumpBC) {
            //FALL-JUMP
            removeConnection(a, b, false)
            removeConnection(b, c, true)
            val dummy = CFGNode.NOOP()
            connect(a, dummy, false)
            connect(dummy, c, true)
        } else {
            //FALL-FALL
            removeConnection(a, b, false)
            removeConnection(b, c, false)
            connect(a, c, false)
        }
    }

    /* Remove a node that is useless.*/
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
        successors[from]?.remove(to, jump)
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
                require(jumpNode == null)
                jumpNode = node
            } else {
                require(fallThrough == null)
                fallThrough = node
            }
        }

        fun remove(node: CFGNode, jump: Boolean) {
            if (jump && jumpNode == node) jumpNode = null
            else if (!jump && fallThrough == node) fallThrough = null
            else {
                throw Exception("WHY ARE YOU REMOVING THIS MY GOOD SIR? ping noah")
            }
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