package optimize.dataflow

import optimize.cfg.CFGNode
import optimize.dataflow.Meet.RealMeet

sealed class Element {
    abstract val meet: Meet<*>

    sealed class Definition : Element() {
        object Top : Definition() {
            override fun toString() = "⊤"
        }

        object Bottom : Definition() {
            override fun toString() = "⊥"
        }

        class Data(val t: Int) : Definition() {
            override fun toString() = t.toString()
        }

        class DesignatedMeeter : Definition()

        override val meet: Meet<Definition> = object : RealMeet<Definition, Data>(Top, Bottom) {
            override fun meetData(e1: Data, e2: Data): Definition = Bottom // multiple definitions means we don't know what it is
        }
    }

    sealed class Unreachability : Element() {
        object Top : Unreachability() {
            override fun toString() = "⊤"
        }

        object Bottom : Unreachability() {
            override fun toString() = "⊥"
        }

        class DesignatedMeeter : Unreachability()

        override val meet: Meet<Unreachability> = object : Meet<Unreachability>(Top, Bottom) {}
    }

    sealed class IntersectNodes : Element() {
        object Top : IntersectNodes() {
            override fun toString() = "⊤"
        }

        object Bottom : IntersectNodes() {
            override fun toString() = "⊥"
        }

        class DesignatedMeeter : IntersectNodes()


        data class Data(val t: Set<CFGNode>) : IntersectNodes() {
            override fun toString(): String {
                return t.map { it.index }.toString()
            }
        }

        override val meet: Meet<IntersectNodes> = object : RealMeet<IntersectNodes, Data>(Top, Bottom) {
            override fun meetData(e1: Data, e2: Data): IntersectNodes = Data(e1.t intersect e2.t)
        }

    }
}