package ast

data class Terminal(
    val line: Int, val column: Int
) {
    init {
        assert(line != -1)
        assert(column != -1)
    }
}