package optimize.cfg

data class Edge(var from: CFGNode, var node: CFGNode, val jump: Boolean = false) {
}