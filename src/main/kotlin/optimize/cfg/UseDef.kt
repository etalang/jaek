package optimize.cfg

import optimize.dataflow.Element

interface UseDef {
    fun use(node: CFGNode): Set<Element.IntersectNodes> {
        return setOf()
    }
    fun def(node: CFGNode): Set<Element.IntersectNodes> {
        return setOf()
    }
}