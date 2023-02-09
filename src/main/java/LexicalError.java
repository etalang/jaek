/** Types of possible errors encounterable while lexing */
enum LexErrType {StringNotEnd, MultilineString, CharWrong, CharNotEnd, UnicodeTooBig}

/** [LexicalError] are exceptions that can be thrown by the lexer while parsing. */
public class LexicalError extends Exception {
    LexErrType errorType;
    int lineNum;
    int col;
    String msg;

    LexicalError(LexErrType lt, int lineNum, int col) {
        errorType = lt;
        switch (lt) {
            case StringNotEnd:
                msg = "Non-terminating string";
                break;
            case CharWrong:
                msg = "Invalid character constant";
                break;
            case CharNotEnd:
                msg = "Unmatched \"'\"";
                break;
            case MultilineString:
                msg = "Multiline string";
                break;
            case UnicodeTooBig:
                msg = "Unicode argument too large";
                break;
        }
        this.lineNum = lineNum;
        this.col = col;
    }
}
