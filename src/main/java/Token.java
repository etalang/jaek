import java.util.ArrayList;

/**
 * A Token consists of the corresponding string lexeme [lexeme], positioning information
 * ([lineNum], [col]), and if applicable, the literal value [attribute]. The attribute should be
 * as accurate as possible to the semantic meaning of the string.
 */
public abstract class Token extends java_cup.runtime.Symbol{
    /** [getStringRepresentation(list)] returns the string representation of an ArrayList of characters */
    final String lexeme;
    int lineNum;
    int col;

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

class StringToken extends Token {
    String attribute;

    StringToken(ArrayList<String> charBuffer) {
        super(getStringRepresentation(charBuffer));
        col = strStart;
        attribute = lex;
    }

    public String toString() {
        return positionInfo() + " string " + attribute;
    }
}

class IntegerToken extends Token {
    long attribute;

    IntegerToken(String lex) {
        super(lex);
        attribute = parseToInt(lex);
    }

    public String toString() {
        return positionInfo() + " integer " + attribute;
    }
}

class CharacterToken extends Token {
    int attribute; // the integer represents the character

    CharacterToken(String lex) throws LexicalError {
        super(lex);
        attribute = parseToChar(lex.substring(1, lex.length() - 1));
    }

    public String toString() {
        return positionInfo() + " character " + formatChar(attribute);
    }
}

class KeywordToken extends Token {
    KeywordToken(String lex) {
        super(lex);
    }
}

class IdToken extends Token {
    IdToken(String lex) {
        super(lex);
    }

    public String toString() {
        return positionInfo() + " id " + lexeme;
    }
}

class SymbolToken extends Token {
    SymbolToken(String lex) {
        super(lex);
    }
}