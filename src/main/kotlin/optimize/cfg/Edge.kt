package optimize.cfg

class Edge(val map: Map<String, CFGNode>, val label: String?) {
    val node: CFGNode? get() = map[label].let { if (it is CFGNode.Cricket) it.to.node else it }
}