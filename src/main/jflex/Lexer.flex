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

    /** Type of tokens in the lexer. */
    enum TokenType {
        RESERVED, SYMBOL, STRING, CHAR, INT, ID
    }

    /** A Token consists of a TokenType [type], the corresponding string lexeme [lexeme], positioning information
     *  ([lineNum], [col]), and if applicable, the literal value [attribute]. The attribute should be
     *  as accurate as possible to the semantic meaning of the string. */
    class Token {
        TokenType type;
        String lexeme;
        int lineNum;
        int col;
        Object attribute;
        Token(TokenType tt, String lex) {
            type = tt; lineNum = lineNumber(); col = column(); lexeme = lex;
            switch (tt) {
                case STRING:
                    attribute = parseToStr(lex); break;
                case INT:
                    attribute = Integer.parseInt(lex); break;
                case CHAR:
                    attribute = parseToChar(lex); break;
                default:
                    attribute = null; break;
            }
        }
        /** [parseToChar(matched)] converts the matched string to a character. */
        // TODO: this might not be right
        public char parseToChar(String matched) {
            // normal case
            if (matched.length() == 3)  {
                return matched.charAt(1);
            }
            // escaped character
            else if (matched.length() == 4) {
                char errorProne = matched.charAt(2); // maybe this is \ or ', "error-prone" escapes
                // newline case
                if (errorProne == 'n')  {
                    return Character.toChars(0x0A)[0];
                }
                else {
                    return errorProne;
                }
            }
            // unicode case
            else {
                // has format "'\x{<stuff>}'"
                String hexNum = "0x" + matched.substring(4, matched.length() - 2);
                int decoded = Integer.decode(hexNum);
                return Character.toChars(decoded)[0];
            }
        }
        /** [parseToStr(matched)] removes the end quote matched by the lexer, and cleans up
         * any unicode characters. */ // TODO: doesn't actually do the unicode thing
        public String parseToStr(String matched) {
            String ret = matched.substring(0, matched.length() - 1);
            return ret;
        }
         /** [TTtoString(tt)] returns the tokentype and literal of the current token, converted to a string
          *  consistent with the test output. */
         public String TTtoString(TokenType tt) {
             switch (tt) {
                 case STRING:
                     return "string " + attribute;
                 case INT:
                     return "integer " + attribute.toString();
                 case CHAR:
                     return "character " + attribute.toString();
                 case ID:
                     return "id " + lexeme; // maybe this is wrong
                 default:
                     return lexeme;
                }
            }
        public String toString() {
            return "" + lineNum + ":" + col + " " + TTtoString(type);
        }
    }
%}

Whitespace = [ \t\f\r\n]
Letter = [a-zA-Z]
Digit = [0-9]
Unicode = "\x{"({Digit}|[a-f]|[A-F]){1,6}"}"
Identifier = {Letter}({Digit}|{Letter}|_|')*
Integer = "0"|[1-9]{Digit}*
Symbol = "-"|"!"|"*"|"*>>"|"/"|"%"|"+"|"<"|"<="|">="|">"|"=="|"!="|"&"|"|"|"("|")"|"["|"]"|"{"|"}"|":"|";"
Reserved = "if"|"return"|"else"|"use"|"while"|"length"|"int"|"bool"|"true"|"false"
Character = "'"([^"\\"]|"\\"|"\\n"|"\\'"|{Unicode})"'"


%state COMMENT
%state STRING

%%

<YYINITIAL> {
    {Whitespace}  { /* ignore */ }
    {Reserved}     { return new Token(TokenType.RESERVED, yytext()); }
    {Identifier}  { return new Token(TokenType.ID, yytext()); }
    {Symbol}    { return new Token(TokenType.SYMBOL, yytext()); }
    {Integer}     { return new Token(TokenType.INT, yytext()); }
    {Character}    { return new Token(TokenType.CHAR, yytext()); }
    "\""        { yybegin(STRING); }
    "//"         { yybegin(COMMENT); }

}
<COMMENT> {
    "\n"  { yybegin(YYINITIAL); }
      [^] { }
}
<STRING> {
    (.|{Unicode})*"\"" { Token t = new Token(TokenType.STRING, yytext()); yybegin(YYINITIAL); return t; }
    [^] {  } // error state
}

[^] {  } // end of file?
