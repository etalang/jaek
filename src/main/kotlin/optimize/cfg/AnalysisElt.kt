package optimize.cfg

sealed class AnalysisElt {
    class VarSet(val set: Set<String>) : AnalysisElt()
}