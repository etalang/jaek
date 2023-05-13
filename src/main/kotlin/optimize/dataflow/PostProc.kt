package optimize.dataflow

import optimize.cfg.CFG
import optimize.cfg.Edge

interface PostProc<Lattice : EdgeValues> {
    fun postprocess(edgeValues: Map<Edge, Lattice>, cfg : CFG)
}