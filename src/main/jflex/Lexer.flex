%%

%public
%class JFlexLexer
%type Token
%function nextToken
%line
%column

%unicode
%pack

%{
    /** Returns the line number the lexer head is currently at in the file, numbered from 1. */
    public int lineNumber() { return yyline + 1; }

    /** Returns the column the lexer head is currently at in the file, numbered from 1. */
    public int column() { return yycolumn + 1; }

    /** Types of possible errors encounterable while lexing */
    enum LexErrType { StringNotEnd, MultilineString, CharWrong, UnexpectedChar }

    /** [LexicalError] are exceptions that can be thrown by the lexer while parsing. */
    class LexicalError extends Exception {
      LexErrType errorType;
      int lineNum;
      int col;
      String msg;

      LexicalError(LexErrType lt) {
          errorType = lt;
          switch (lt)  {
              case StringNotEnd:
                  msg = "Non-terminating string"; break;
              case CharWrong:
                  msg = "Invalid character constant"; break;
              case UnexpectedChar:
                  msg = "Unexpected character"; break;
          }
          lineNum = lineNumber(); col = column();
      }
    }

    /** [isPrintable(n)] checks if n is an integer whose corresponding character is printable. For
    * ASCII, this is 32-126. Requires n >= 0.  */
    public boolean isPrintable(int n) {
        if (n < 32 || n == 127) {
            return false;
        }
        return true;
    }

    /** A Token consists of the corresponding string lexeme [lexeme], positioning information
     *  ([lineNum], [col]), and if applicable, the literal value [attribute]. The attribute should be
     *  as accurate as possible to the semantic meaning of the string. */
    abstract class Token {
        final String lexeme;
        int lineNum;
        int col;
        Token(String lex) {
            lineNum = lineNumber(); col = column(); lexeme = lex;
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
        StringToken(String lex)  {
            super(lex);
            col = column() - 1; // need this to offset missing quotation
            attribute = parseToStr(lex);
        }
        /** [parseToStr(matched)] removes the end quote matched by the lexer, and cleans up
        * any unicode characters. */ // TODO: the unicode replacement can definitely be done more cleanly
        public String parseToStr(String matched) {
            String ret = matched.substring(0, matched.length() - 1);
            while (ret.contains("\\x{")) {
                int unicodeIdx = ret.indexOf("\\x{");
                int endUnicode = ret.indexOf("}", unicodeIdx);
                int codePoint = Integer.parseInt(ret.substring(unicodeIdx + 3, endUnicode), 16);
                if (isPrintable(codePoint)) {
                    ret = ret.substring(0, unicodeIdx) + (char) (codePoint) + ret.substring(endUnicode + 1);
                }
            }
            return ret;
        }
        public String toString() {
            return positionInfo() + " string " + attribute;
        }
    }

    class IntegerToken extends Token {
        int attribute;
        IntegerToken(String lex)  {
            super(lex);
            attribute = Integer.parseInt(lex);
        }
        public String toString() {
            return positionInfo() + " integer " + attribute;
        }
    }

    class CharacterToken extends Token {
        int attribute; // the integer represents the character
        CharacterToken(String lex)  {
            super(lex);
            attribute = parseToChar(lex);
        }
        /** [parseToChar(matched)] converts the matched string to the integer representing
        * the character. Throws an LexicalError if the string does not correspond to a
        * character. */
        // TODO: this might not be right
        public int parseToChar(String matched) {
            // normal case
            if (matched.length() == 3)  {
                return matched.charAt(1);
            }
            // escaped character
            else if (matched.length() == 4) {
                char errorProne = matched.charAt(2); // maybe this is \ or ', "error-prone" escapes
                // newline case
                if (errorProne == 'n')  {
                    return 0x0A;
                }
                else { // extract the character
                    return errorProne;
                }
            }
            // unicode case
            else {
                // has format "'\x{<stuff>}'"
                int hexNum = Integer.parseInt(matched.substring(4, matched.length() - 2), 16);
                return hexNum;
            }
        }

        public String toString() {
            return positionInfo() + " character " + (char) attribute;
        }
    }

    class KeywordToken extends Token {
        KeywordToken(String lex)  {
            super(lex);
        }
    }

    class IdToken extends Token {
        IdToken(String lex)  {
            super(lex);
        }
        public String toString() {
            return positionInfo() + " id " + lexeme;
        }
    }

    class SymbolToken extends Token {
        SymbolToken(String lex)  {
            super(lex);
        }
    }


%}

Whitespace = [ \t\f\r\n]
Letter = [a-zA-Z]
Digit = [0-9]
Unicode = "\x{"({Digit}|[a-f]|[A-F]){1,6}"}"
Identifier = {Letter}({Digit}|{Letter}|_|')*
Integer = "0"|[1-9]{Digit}*
Symbol = "-"|"!"|"*"|"*>>"|"/"|"%"|"+"|"<"|"<="|">="|">"|"=="|"!="|"="|"&"|"|"|"("|")"|"["|"]"|"{"|"}"|":"|";"
Reserved = "if"|"return"|"else"|"use"|"while"|"length"|"int"|"bool"|"true"|"false"
Character = "'"([^"\\"]|"\\"|"\\n"|"\\'"|{Unicode})"'"

%state COMMENT
%state STRING

%%

<YYINITIAL> {
    {Whitespace}  { /* ignore */ }
    {Reserved}     { return new KeywordToken(yytext()); }
    {Identifier}  { return new IdToken(yytext()); }
    {Symbol}    { return new SymbolToken(yytext()); }
    {Integer}     { return new IntegerToken(yytext()); }
    {Character}    { return new CharacterToken( yytext()); }
    "\""        { yybegin(STRING); }
    "//"         { yybegin(COMMENT); }
// unmatched single quote error?
}
<COMMENT> {
    "\n"  { yybegin(YYINITIAL); }
      [^] { }
}
<STRING> {
    (.|{Unicode})*"\"" { Token t = new StringToken(yytext()); yybegin(YYINITIAL); return t; }
    [^] {  } // error state
}

[^] {  } // end of file?
