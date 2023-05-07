package optimize.dataflow

import optimize.cfg.CFGNode
import optimize.dataflow.Meet.RealMeet
import kotlin.math.max

sealed class Element {
    abstract val meet: Meet<*>

    sealed class UpperBounds : Element() {
        object Top : UpperBounds()
        object Bottom : UpperBounds()

        class Data(val t: Int) : UpperBounds()

        override val meet: Meet<UpperBounds> = object : RealMeet<UpperBounds, Data>(Top, Bottom) {
            override fun meetData(e1: Data, e2: Data): UpperBounds = Data(max(e1.t, e2.t))
        }
    }

    sealed class Reachability : Element() {
        object Top : Reachability()
        object Bottom : Reachability()

        override val meet: Meet<Reachability> = object : Meet<Reachability>(Top, Bottom) {}
    }

    sealed class IntersectNodes : Element() {
        object Top : IntersectNodes()
        object Bottom : IntersectNodes()

        class Data(val t: Set<CFGNode>) : IntersectNodes()

        override val meet: Meet<IntersectNodes> = object : RealMeet<IntersectNodes, Data>(Top, Bottom) {
            override fun meetData(e1: Data, e2: Data): IntersectNodes = Data(e1.t intersect e2.t)
        }
}