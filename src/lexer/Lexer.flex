// EXAMPLE FILE
package lexer;
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
    enum LexErrType { StringNotEnd, CharWrong, UnexpectedChar }

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
     *  ([lineNum], [col]), and if applicable, the literal value [attribute]. */
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
                    attribute = lex; break; // TODO: Handle string parsing correctly
                case INT:
                    attribute = Integer.parseInt(lex); break;
                case CHAR:
                    attribute = 'a'; break; // TODO: Handle character parsing correctly
                default:
                    attribute = null; break;
            }
        }
         /** [TTtoString(tt)] returns the tokentype and literal of the current token, converted to a string
          *  consistent with the test output. */
         public String TTtoString(TokenType tt) {
             switch (tt) {
                 case STRING:
                     return "string " + lexeme.substring(1, lexeme.length() - 1); // TODO: make sure this prints properly (escaped unicode)
                 case INT:
                     return "integer " + attribute.toString();
                 case CHAR:
                     return "character " + attribute.toString(); // TODO: make sure this prints properly (escaped unicode)
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
<STRING> { // TODO: String matching is broken? someone should look at this matching
    (.|{Unicode})*"\"" { return new Token(TokenType.STRING, yytext()); yybegin(YYINITIAL); }
    [^] {  } // error state
}

[^] {  } // end of file?
