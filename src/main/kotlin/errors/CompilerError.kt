package errors

import java.io.File

sealed class CompilerError(val line: Int, val column: Int, message: String, val file: File) : Exception(message) {
    abstract val mini: String
    abstract val log: String
}