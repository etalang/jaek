package optimize.cfg


sealed class CFGFlow<lattice> {
    abstract val values: Map<Edge, lattice>
    abstract fun meet(e1: lattice, e2: lattice): lattice
    abstract fun transition(n: CFGNode, info:lattice)
    abstract fun run()

    abstract class Forward<lattice> : CFGFlow<lattice>(){
        override fun run() {
            TODO("Not yet implemented")
        }
    }
}