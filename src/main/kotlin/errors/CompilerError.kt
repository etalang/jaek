package errors

sealed class CompilerError(val line: Int, val column: Int, message: String, val file: String) : Exception(message) {
    abstract val mini: String
    abstract val log: String
}