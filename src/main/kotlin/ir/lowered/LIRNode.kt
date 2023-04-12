package ir.lowered

import assembly.Tile
import ir.InterRep

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
}