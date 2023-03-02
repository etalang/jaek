package errors

sealed class CompilerError(val line: Int, val column: Int, message: String) : Exception(message)
{
    @Deprecated("reverse deprecated -- experimental!")
    abstract fun output(file : String) : String
}