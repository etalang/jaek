package optimize.dataflow

import optimize.cfg.CFGNode
import optimize.dataflow.Meet.RealMeet
import kotlin.math.max

sealed class Element {
    abstract val meet: Meet<*>
    abstract val pretty: String

    sealed class UpperBounds : Element() {
        object Top : UpperBounds() {
            override val pretty = "⊤"
        }

        object Bottom : UpperBounds() {
            override val pretty = "⊥"
        }

        class Data(val t: Int) : UpperBounds() {
            override val pretty = "stfu hoe"
        }

        override val meet: Meet<UpperBounds> = object : RealMeet<UpperBounds, Data>(Top, Bottom) {
            override fun meetData(e1: Data, e2: Data): UpperBounds = Data(max(e1.t, e2.t))
        }
    }

    sealed class Reachability : Element() {
        object Top : Reachability() {
            override val pretty = "⊤"
        }

        object Bottom : Reachability() {
            override val pretty = "⊥"
        }


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