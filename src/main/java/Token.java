import java_cup.runtime.Symbol;

/**
 * A Token consists of the corresponding string lexeme [lexeme], positioning information
 * ([lineNum], [col]), and if applicable, the literal value [attribute]. The attribute should be
 * as accurate as possible to the semantic meaning of the string.
 */
public abstract class Token<T> extends Symbol {
    final String lexeme;
    private final int lineNum;
    private final int col;

    protected T attribute;

    Token(String lex, int lineNum, int col) {
        //TODO: DO CORRECTLY
        super(0);
        this.lineNum = lineNum;
        this.col = col;
        lexeme = lex;
    }

    public String positionInfo() {
        return "" + lineNum + ":" + col;
    }

    public String toString() {
        return positionInfo() + " " + lexeme;
    }

}

class StringToken extends Token<String> {

    StringToken(String lex, int lineNum, int strStart) {
        super(lex, lineNum, strStart);
        attribute = lex;
    }

    public String toString() {
        return positionInfo() + " string " + attribute;
    }
}

class IntegerToken extends Token<Long> {
    IntegerToken(String lex, int lineNum, int col) {
        super(lex, lineNum, col);
        attribute = LexUtil.parseToInt(lex);
    }

    public String toString() {
        return positionInfo() + " integer " + attribute;
    }
}

class CharacterToken extends Token<Integer> {

    CharacterToken(String lex, int lineNum, int col) throws LexicalError {
        super(lex, lineNum, col);
        attribute = LexUtil.parseToChar(lex.substring(1, lex.length() - 1), lineNum, col);
    }

    public String toString() {
        return positionInfo() + " character " + LexUtil.formatChar(attribute);
    }
}

class KeywordToken extends Token<Void> {
    KeywordToken(String lex, int lineNum, int col) {
        super(lex, lineNum, col);
    }
}

class IdToken extends Token<Void> {
    IdToken(String lex, int lineNum, int col) {
        super(lex, lineNum, col);
    }

    public String toString() {
        return positionInfo() + " id " + lexeme;
    }
}

class SymbolToken extends Token<Void> {
    SymbolToken(String lex, int lineNum, int col) {
        super(lex, lineNum, col);
    }
}