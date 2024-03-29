import errors.LexicalError;
import java_cup.runtime.Symbol;

import java.io.File;

/**
 * A Token consists of the corresponding positioning information ([lineNum], [col]),
 * and if applicable, the literal value [attribute]. The attribute should be
 * as accurate as possible to the semantic meaning of the string.
 */
public abstract class Token<T> extends Symbol {
    protected final T attribute;
    private final int lineNum;
    private final int col;

    public Token(T attribute, int sym, int lineNum, int col) {
        super(sym, lineNum, col, attribute); // long story short I do not like this but it's basically impossible to get the Symbol on the left of the ::= to CORRECTLY USE left and right or to even DOWNCAST Symbol to Token<*> to get lineNum and col so here we are :(
        this.attribute = attribute;
        this.lineNum = lineNum;
        this.col = col;
    }

    public String location() {
        return lineNum + ":" + col;
    }

    public int getLine() {
        return lineNum;
    }

    public int getCol() {
        return col;
    }

    public String lexInfo() {
        return location() + " " + type() + stringVal();
    }

    public String stringVal() {
        return attribute.toString();
    }

    protected String type() {
        return "";
    }

    static class StringToken extends Token<String> {
        public StringToken(String attribute, int lineNum, int col) {
            super(attribute, SymbolTable.STRING_LITERAL, lineNum, col);
        }

        @Override
        protected String type() {
            return "string ";
        }
    }

    static class IntegerToken extends Token<String> {
        public IntegerToken(String attribute, int lineNum, int col, File file) throws LexicalError {
            super(LexUtil.parseToInt(attribute, lineNum, col, file), SymbolTable.INTEGER_LITERAL, lineNum, col);
        }

        @Override
        protected String type() {
            return "integer ";
        }
    }

    static class CharacterToken extends Token<Integer> {
        CharacterToken(String lex, int lineNum, int col, File file) throws LexicalError {
            super(LexUtil.parseToChar(lex.substring(1, lex.length() - 1), lineNum, col, file), SymbolTable.CHARACTER_LITERAL, lineNum, col);
        }

        @Override
        protected String type() {
            return "character ";
        }

        @Override
        public String stringVal() {
            return LexUtil.formatChar(attribute);
        }
    }

    static class KeywordToken extends Token<String> {
        public KeywordToken(String attribute, int lineNum, int col) {
            super(attribute, LexUtil.getSym(attribute), lineNum, col);
        }
    }

    static class IdToken extends Token<String> {
        public IdToken(String attribute, int lineNum, int col) {
            super(attribute, SymbolTable.IDENTIFIER, lineNum, col);
        }

        @Override
        protected String type() {
            return "id ";
        }
    }

    static class SymbolToken extends Token<String> {
        public SymbolToken(String attribute, int lineNum, int col) {
            super(attribute, LexUtil.getSym(attribute), lineNum, col);
        }
    }

    static class EOFToken extends Token<String> {
        public EOFToken(int lineNum, int col) {
            super("<<EOF>>", SymbolTable.EOF, lineNum, col);
        }
    }
}