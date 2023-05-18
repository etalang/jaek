package optimize.dataflow

//this is the type of shit that belongs in official java source code that you do not touch
abstract class Meet<T>(val top: T, val bottom: T) {
    abstract class RealMeet<T, D : T>(top: T, bottom: T) : Meet<T>(top, bottom) {
        abstract fun meetData(e1: D, e2: D): T

        override fun meet(e1: T, e2: T): T {
            if (e1 == bottom || e2 == bottom) return bottom
            if (e1 == top) return e2
            if (e2 == top) return e1
            @Suppress("UNCHECKED_CAST") // not suspicious at all
            return meetData(e1 as D, e2 as D)
        }
    }

    open fun meet(e1: T, e2: T): T {
        if (e1 == bottom || e2 == bottom) return bottom
        if (e1 == top) return e2
        if (e2 == top) return e1
        throw Exception("keep your data outta my trash")
    }

}