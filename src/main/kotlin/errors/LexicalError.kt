package errors

/** [LexicalError] are exceptions that can be thrown by the lexer while parsing.  */
class LexicalError(private val errorType: errType, line: Int, col: Int, file: String) : CompilerError(
    line, col, "Lexical Error", file
) {
    override val mini: String = line.toString() + ":" + col + " error:" + details()

    fun details(): String {
        return when (errorType) {
            errType.BadString -> "Non-terminating string or multiline string"
            errType.CharWrong -> "Invalid character constant"
            errType.CharNotEnd -> "Unmatched \"'\""
            errType.UnicodeTooBig -> "Unicode argument too large"
            errType.InvalidId -> "Not a valid identifier"
            errType.InvalidInteger -> "Not a valid integer"
        }
    }

    /** Types of possible errors encounterable while lexing  */
    enum class errType {
        BadString, CharWrong, CharNotEnd, UnicodeTooBig, InvalidId, InvalidInteger
    }

    override val log: String =
        "Lexical error beginning at ${file}:${line}:${column}: ${details()}"
}