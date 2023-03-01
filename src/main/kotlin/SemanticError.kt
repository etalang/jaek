class SemanticError(val line : Int, val col : Int, val desc : String) : RuntimeException() {
}