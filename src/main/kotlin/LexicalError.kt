/** Types of possible errors encounterable while lexing  */
enum class LexErrType {
    StringNotEnd, MultilineString, CharWrong, CharNotEnd, UnicodeTooBig
}

/** [LexicalError] are exceptions that can be thrown by the lexer while parsing.  */
class LexicalError internal constructor(var errorType: LexErrType, lineNum: Int, col: Int) : Exception() {
    var lineNum: Int
    var col: Int
    var msg: String? = null

    init {
        msg = when (errorType) {
            LexErrType.StringNotEnd -> "Non-terminating string"
            LexErrType.CharWrong -> "Invalid character constant"
            LexErrType.CharNotEnd -> "Unmatched \"'\""
            LexErrType.MultilineString -> "Multiline string"
            LexErrType.UnicodeTooBig -> "Unicode argument too large"
        }
        this.lineNum = lineNum
        this.col = col
    }
}