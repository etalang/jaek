package optimize.cfg

class Edge(val from : CFGNode, var node: CFGNode, val jump: Boolean = false) {
    class Lazy(val map: Map<String, CFGNode>, val label: String?) {
        val node: CFGNode? get() = map[label].let { if (it is CFGNode.Cricket) it.lazyTo.node else it }
        fun toReal(from : CFGNode, forceJump:Boolean = false): Edge? {
            var target = map[label]
            var foundJump = forceJump;
            while (target is CFGNode.Cricket) {
                foundJump = true
                target = map[target.lazyTo.label]
            }
            return target?.let { Edge(from, target, foundJump) }
        }
    }
}