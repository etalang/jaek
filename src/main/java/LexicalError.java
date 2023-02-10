/** [LexicalError] are exceptions that can be thrown by the lexer while parsing. */
public class LexicalError extends Exception {

    private final errType errorType;
    private final int lineNum;
    private final int col;

    LexicalError(errType lt, int lineNum, int col) {
        super("Lexical Error at " + lineNum + ":" + col);
        this.errorType = lt;
        this.lineNum = lineNum;
        this.col = col;
    }

    public String getMsg() {
        return lineNum + ":" + col + " error:" + details();
    }

    private String details() {
        switch (errorType) {
            case StringNotEnd:
                return "Non-terminating string";
            case CharWrong:
                return "Invalid character constant";
            case CharNotEnd:
                return "Unmatched \"'\"";
            case MultilineString:
                return "Multiline string";
            case UnicodeTooBig:
                return "Unicode argument too large";
            default:
                return "";
        }
    }

    /** Types of possible errors encounterable while lexing */
    public enum errType {StringNotEnd, MultilineString, CharWrong, CharNotEnd, UnicodeTooBig}
}
