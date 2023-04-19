package ir.lowered

import assembly.Tile
import assembly.x86.Memory.RegisterMem
import assembly.x86.Register.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import ir.InterRep
import java.util.Queue
import java.util.LinkedList

/** IRNode represents a node in the intermediate representation abstract syntax tree**/
sealed class LIRNode : InterRep() {
    sealed class TileableNode<TileType> : LIRNode() where TileType : Tile {
        fun optimalTile(): TileType {
            return bestTile ?: let {
                bestTile = defaultTile
                findBestTile(); bestTile ?: defaultTile.let { default ->
                bestTile = default; default
            }
            }
        }

        private var bestTile: TileType? = null
        protected fun attempt(tile: TileType?) {
            if (tile != null && tile.cost < (bestTile?.cost ?: Int.MAX_VALUE)) bestTile = tile
        }

        abstract val defaultTile: TileType
        protected abstract fun findBestTile()
    }

    /** detectMemoryFriendly(n) returns a RegisterMem with the address given by n if n is an LIRExpr structured as a
     * valid memory address and null otherwise. */
    fun detectMemoryFriendly(n : LIRExpr) : RegisterMem? {
        if (n is LIROp) {
            if (n.op == IRBinOp.OpType.ADD) {
                return detectMFAddends(n)
            }
            else if (n.op == IRBinOp.OpType.MUL) {
                return detectMFFactors(n)
            }
        }
        return null // RegisterMem(Abstract.freshRegister(), Abstract.freshRegister())
    }

    private val validScalars : Set<Long> = setOf(1L, 2L, 4L, 8L)

    // v: valid shift (1, 2, 4, 8)
    // c: constant offset
    // ti: temps
    /** detectMFFactors attempts to create a RegisterMem object with the factors of a LIROp.Mul expression, and returns
     * it if it is successful and returns null otherwise.  */
    private fun detectMFFactors(parent : LIROp) : RegisterMem? { // only [v * t2]
        val validScale : Long
        val scaled : String
        val leftChild = parent.left
        val rightChild = parent.right
        if (leftChild is LIRExpr.LIRConst && rightChild is LIRExpr.LIRTemp) {
            validScale = leftChild.value
            scaled = rightChild.name
        }
        else if (rightChild is LIRExpr.LIRConst && leftChild is LIRExpr.LIRTemp) {
            validScale = rightChild.value
            scaled = leftChild.name
        }
        else {
            return null
        }
        val shift = convertToShift(validScale)
        return if (shift == null) null
        else RegisterMem(null, Abstract(scaled), shift = shift)
    }

    /** detectMFFactors attempts to create a RegisterMem object with the factors of a LIROp.Mul expression, and returns
     * it if it is successful and returns null otherwise.  */
    private fun detectMFAddends(parent : LIROp) : RegisterMem? {
        var validScale : Long? = null
        var scaled : String? = null
        var const : Long? = null
        val addedTemps = mutableListOf<String>()
        val childrenQueue : Queue<LIRExpr> = LinkedList()
        childrenQueue.add(parent.left)
        childrenQueue.add(parent.right)
        while (childrenQueue.isNotEmpty()) {
            val child = childrenQueue.poll()
            when (child) {
                is LIRExpr.LIRConst -> {
                    if (const != null) return null
                    const = child.value
                }
                is LIRExpr.LIRTemp -> {
                    addedTemps.add(child.name)
                }
                is LIROp -> {
                    if (child.op == IRBinOp.OpType.ADD) {
                        childrenQueue.add(child.left)
                        childrenQueue.add(child.right)
                    }
                    else if (child.op == IRBinOp.OpType.MUL) {
                        val left = child.left
                        val right = child.right
                        if (validScale != null || scaled != null) return null
                        if (left is LIRExpr.LIRConst && right is LIRExpr.LIRTemp) {
                            validScale = left.value
                            scaled = right.name
                        }
                        else if (right is LIRExpr.LIRConst && left is LIRExpr.LIRTemp) {
                            validScale = right.value
                            scaled = left.name
                        }
                        else {
                            return null
                        }
                    }
                    else return null
                }
                is LIRMem, is LIRExpr.LIRName -> return null
            }
        }
        if (addedTemps.size > 2) return null
        if (addedTemps.size == 2 && validScale == null && const == null && scaled == null) { // [t1 + t2]
            return RegisterMem(Abstract(addedTemps[0]), Abstract(addedTemps[1]))
        }
        if (addedTemps.size == 1 && validScale != null && scaled != null && const == null) { // [t1 + v * t2]
            val shift = convertToShift(validScale)
            return if (shift == null) null
            else RegisterMem(Abstract(addedTemps[0]), Abstract(scaled), shift = shift)
        }
        if (addedTemps.size == 1 && validScale == null && scaled == null && const != null) { // [t1 + c]
            return RegisterMem(Abstract(addedTemps[0]), null, offset = const)
        }
        if (addedTemps.size == 2 && validScale == null && scaled == null && const != null) { // [t1 + t2 + c]
            return RegisterMem(Abstract(addedTemps[0]), Abstract(addedTemps[1]), offset = const)
        }
        if (addedTemps.size == 1 && validScale != null && scaled != null && const != null) { // [t1 + v * t2 + c]
            val shift = convertToShift(validScale)
            return if (shift == null) null
            else RegisterMem(Abstract(addedTemps[0]), Abstract(scaled), shift = shift, offset = const)
        }
        if (addedTemps.size == 0 && validScale != null && scaled != null && const != null) { // [v * t2 + c]
            val shift = convertToShift(validScale)
            return if (shift == null) null
            else RegisterMem(null, Abstract(scaled), shift = shift, offset = const)
        }
        return null
    }

    private fun convertToShift(n : Long) : RegisterMem.Shift? {
        return when (n) {
            1L -> RegisterMem.Shift.ONE
            2L -> RegisterMem.Shift.TWO
            4L -> RegisterMem.Shift.FOUR
            8L -> RegisterMem.Shift.EIGHT
            else -> null
        }
    }

}