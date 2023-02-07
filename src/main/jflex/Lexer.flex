import java.util.ArrayList;
import java.math.BigInteger;

%%

%public
%class JFlexLexer
%type Token
%function nextToken
%line
%column
%yylexthrow LexicalError

%unicode
%pack

%{
    /** global character array consisting of characters to be read in for a string */
    ArrayList<Integer> charBuffer;
    int strStart;

    /** [formatChar(n)] outputs the printable version of a Character. */
    private static String formatChar(Integer character) {
        if (character == 10) return "\\n";
        if (character < 32 || character >= 127) {
            return "\\x{" + Integer.toHexString(character) + "}";
        }
        int charTruncated = character % (1 << 16);
        char asciiChar = (char) charTruncated;
        return Character.toString(asciiChar);
    }

    /** Returns the line number the lexer head is currently at in the file, numbered from 1. */
    public int lineNumber() {
        return yyline + 1;
    }

    /** Returns the column the lexer head is currently at in the file, numbered from 1. */
    public int column() {
        return yycolumn + 1;
    }

    /** [getStringRepresentation(list)] returns the string representation of an ArrayList of characters */
    String getStringRepresentation(ArrayList<Integer> list) {
        StringBuilder builder = new StringBuilder(list.size());
        for (Integer ch : list) {
            builder.append(formatChar(ch));
        }
        return builder.toString();
    }

    /**
     * [parseToChar(matched)] converts the matched string to the integer representing
     * the character. Throws an LexicalError if the string does not correspond to a
     * character.
     */
    public int parseToChar(String matched) throws LexicalError {
        // normal case
        if (matched.length() == 1) {
            return (int) matched.charAt(0);
        }
        // escaped character
        else if (matched.length() == 2) {
            char errorProne = matched.charAt(1); // maybe this is \ or ', "error-prone" escapes
            // newline case
            if (errorProne == 'n') {
                return 0x0A;
            } else { // extract the character
                return (int) errorProne;
            }
        }
        // unicode case
        else if (matched.length() >= 5 && "\\x{".equals(matched.substring(0, 3))) {
            // has format "\x{<stuff>}"
            int hexNum = Integer.parseInt(matched.substring(3, matched.length() - 1), 16);
            if (hexNum < 0 || hexNum >= 1 << 24) {
                throw new LexicalError(LexErrType.UnicodeTooBig);
            }
            return hexNum;
        } else {
            throw new LexicalError(LexErrType.CharWrong);
        }
    }

    /**
     * [parseToInt(matched)] truncates matched to fit into a long. If the number is too large, it will
     * be taken mod 2^64 and shifted to fit into the correct long range. In the specific case
     */
    public long parseToInt(String matched) {
        if (matched.length() <= 18) { // there are 19 digits in 2^63
            return Long.parseLong(matched);
        } else {
            BigInteger bi = new BigInteger(matched);
            return bi.longValue();
        }
    }

    /** Types of possible errors encounterable while lexing */
    enum LexErrType {StringNotEnd, MultilineString, CharWrong, CharNotEnd, UnicodeTooBig}

    /** [LexicalError] are exceptions that can be thrown by the lexer while parsing. */
    class LexicalError extends Exception {
        LexErrType errorType;
        int lineNum;
        int col;
        String msg;

        LexicalError(LexErrType lt) {
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
            lineNum = lineNumber();
            col = column();
        }
    }

    /**
     * A Token consists of the corresponding string lexeme [lexeme], positioning information
     * ([lineNum], [col]), and if applicable, the literal value [attribute]. The attribute should be
     * as accurate as possible to the semantic meaning of the string.
     */
    abstract class Token {
        final String lexeme;
        int lineNum;
        int col;

        Token(String lex) {
            lineNum = lineNumber();
            col = column();
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

        StringToken(String lex) {
            super(lex);
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
%}

Whitespace = [ \t\f\r\n]
Letter = [a-zA-Z]
Digit = [0-9]
Unicode = "\\x{"({Digit}|[a-f]|[A-F]){1,6}"}"
Identifier = {Letter}({Digit}|{Letter}|_|')*
Integer = "0"|[1-9]{Digit}*
Character = [^"\\""'"]|"\\"("\\"|"\""|"'"|"n")|{Unicode}
Symbol = "-"|"!"|"*"|"*>>"|"/"|"%"|"+"|"_"|"<"|"<="|">="|","|">"|"=="|"!="|"="|"&"|"|"|"("|")"|"["|"]"|"{"|"}"|":"|";"
Reserved = "if"|"return"|"else"|"use"|"while"|"length"|"int"|"bool"|"true"|"false"
CharLiteral = "'"({Character}|"\"")"'"

%state COMMENT
%state STRING

%%

<YYINITIAL> {
    {Whitespace}  { /* ignore */ }
    {Reserved}     { return new KeywordToken(yytext()); }
    {Identifier}  { return new IdToken(yytext()); }
    {Symbol}    { return new SymbolToken(yytext()); }
    {Integer}     { return new IntegerToken(yytext()); }
    {CharLiteral}    { return new CharacterToken( yytext()); }
    "\""        { charBuffer = new ArrayList<Integer>(); strStart = column(); yybegin(STRING); }
    "//"         { yybegin(COMMENT); }
    "'"           { throw new LexicalError(LexErrType.CharNotEnd);}
}
<COMMENT> {
    "\n"  { yybegin(YYINITIAL); }
      [^] { }
}
<STRING> {
    "\""               { Token t = new StringToken(getStringRepresentation(charBuffer));
                            yybegin(YYINITIAL); return t; }
    "\n"          { throw new LexicalError(LexErrType.MultilineString); }
    ({Character}|"'")  { int c = parseToChar(yytext()); charBuffer.add(c); }
    [^]                {throw new LexicalError(LexErrType.StringNotEnd); }
}

[^] {  } // end of file?
