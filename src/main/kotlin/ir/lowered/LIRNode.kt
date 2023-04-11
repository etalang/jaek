package ir.lowered

import assembly.tile.BuiltTile
import ir.InterRep

/** IRNode represents a node in the intermediate representation abstract syntax tree**/
sealed class LIRNode : InterRep() {
    sealed class TileableNode<TileType> : LIRNode() where TileType : BuiltTile {
        val optimalTile: TileType get() = bestTile ?: let { findBestTile(); bestTile ?: defaultTile }
        private var bestTile: TileType? = null
        protected fun attempt(tile: TileType?) {
            if (tile != null && tile.cost < (bestTile?.cost ?: Int.MAX_VALUE)) bestTile = tile
        }

        abstract val defaultTile: TileType
        protected abstract fun findBestTile()
    }
}