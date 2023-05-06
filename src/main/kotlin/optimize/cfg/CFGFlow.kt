package optimize.cfg

abstract class CFGFlow {
    abstract val inMap : Map<CFGNode, AnalysisElt>
    abstract val outMap : Map<CFGNode, AnalysisElt>
    abstract val top : AnalysisElt // not always going to want strings here.... make a more sophisticated elt type
    // additionally consider that this is not always even a set or predictable - consider CCP
    abstract fun meet(e1:AnalysisElt, e2:AnalysisElt) : AnalysisElt
    abstract fun transition(n : CFGNode)

    fun run (){}
}