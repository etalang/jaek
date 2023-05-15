package optimize.cfg

class Edge(var from: CFGNode, var node: CFGNode, val jump: Boolean = false) {
    class Lazy(val map: Map<String, CFGNode>, val label: String?) {
        fun toReal(from: CFGNode, forceJump: Boolean = false): Edge? {
            var target = map[label]
//            var foundJump = forceJump
//            val peskyBugs = if (from is CFGNode.Cricket) mutableSetOf<CFGNode.Cricket>(from) else mutableSetOf()
//            while (target is CFGNode.Cricket) {
//                if (peskyBugs.contains(target)) {
//                    //damn we in a cycle fo real
//                    return Edge(from, from, true)
//
//                } else {
//                    peskyBugs.add(target)
//                    foundJump = true
//                    target = map[target.lazyTo.label]
//                }
//            }
            return target?.let { Edge(from, it, forceJump || from is CFGNode.Cricket) }
        }
    }

    fun copy(from: CFGNode = this.from, node: CFGNode = this.node, jump: Boolean = this.jump): Edge {
        return Edge(from, node, jump)
    }
}