package optimize.cfg


sealed class CFGFlow<lattice>(val cfg: CFG) {
    abstract val values: MutableMap<Edge, lattice>
    abstract fun meet(e1: lattice, e2: lattice): lattice
    abstract fun transition(n: CFGNode, inInfo: lattice): Map<Edge, lattice>
    abstract val top: lattice
    abstract fun run()

    abstract class Forward<lattice>(cfg: CFG) : CFGFlow<lattice>(cfg) {
        override fun run() {
            TODO("Not yet implemented")
        }
    }

}