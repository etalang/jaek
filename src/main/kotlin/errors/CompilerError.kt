package errors

sealed class CompilerError(val line: Int, val column: Int, message: String) : Exception(message) {
    abstract val mini: String
    abstract fun log(file: String): String
}