package optimize.cfg

class MatchMaker(val start: CFGNode, private val constructionMap: Map<String, CFGNode>) {
    /* node to nodes leading into it */
    private val predecessors: MutableMap<CFGNode, MutableSet<Pair<CFGNode, Boolean>>> = mutableMapOf()
    /* node to fall-throughs and jumps */
    private val successors: MutableMap<CFGNode, Successors> = mutableMapOf()

    fun allNodes(): Set<CFGNode> {
        return predecessors.keys union successors.keys
    }

    fun fastNodesWithPredecessors(): Set<CFGNode> {
        return predecessors.keys
    }

    fun build(from: CFGNode, to: String, jump: Boolean) {
        constructionMap[to]?.let { connect(from, it, jump) }
    }

    fun connect(from: CFGNode, to: CFGNode, jump: Boolean) {
        successors.computeIfAbsent(from) { Successors() }.set(to, jump)
        predecessors.computeIfAbsent(to) { mutableSetOf() }.add(Pair(from, jump))
    }

    fun remove(from: CFGNode, to: CFGNode, jump: Boolean) {
        //TODO IDK IF THIS WORKS
        predecessors[to]?.removeIf { (node, _) -> node == from }
        successors[from]?.remove(to)
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

    fun predecessorEdges(node: CFGNode): Set<Edge> {
        return predecessors[node]?.map { Edge(it.first, node, it.second) }?.toSet() ?: emptySet()
    }

    class Successors {
        var fallThrough: CFGNode? = null
        var jumpNode: CFGNode? = null
        fun set(node: CFGNode, jump: Boolean) {
            if (jump) {
                if (jumpNode != null) throw Exception("cannot override jump!")
                jumpNode = node
            } else {
                if (fallThrough != null) throw Exception("cannot override fall through!")
                fallThrough = node
            }
        }

        fun remove(node: CFGNode) {
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
    }
}