// EXAMPLE FILE

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
    /** Type of tokens in the lexer. */
    enum TokenType {
        RESERVED, SYMBOL, STRING, CHAR, INT, ID
    }

    /** Returns the line number the lexer head is currently at in the file, numbered from 1. */
    public int lineNumber() { return yyline + 1; }

    /** Returns the column the lexer head is currently at in the file, numbered from 1. */
    public int column() { return yycolumn + 1; }

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
                    attribute = lex; break;
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
                     return "string " + lexeme.substring(1, lexeme.length() - 1);
                 case INT:
                     return "integer " + attribute.toString();
                 case CHAR:
                     return "character " + attribute.toString();
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
Identifier = {Letter}({Digit}|{Letter}|_|')*
Integer = "0"|"-"?[1-9]{Digit}*|"\x"
Symbol = "-"|"!"|"*"|"*>>"|"/"|"%"|"+"|"<"|"<="|">="|">"|"=="|"!="|"&"|"|"|"("|")"|"["|"]"|"{"|"}"|":"|";"
Reserved = "if"|"return"|"else"|"use"|"while"|"length"|"int"|"bool"|"true"|"false"
Character = "'"([^"\\"]|"\\"|"\\n"|"\\'")"'"
String = "\"".*"\""

%state COMMENT


%%

<YYINITIAL> {
    {Whitespace}  { /* ignore */ }
    {Reserved}     { return new Token(TokenType.RESERVED, yytext()); }
    {Identifier}  { return new Token(TokenType.ID, yytext()); }
    {Symbol}    { return new Token(TokenType.SYMBOL, yytext()); }
    {Integer}     { return new Token(TokenType.INT, yytext()); }
    {Character}    { return new Token(TokenType.CHAR, yytext()); }
    {String}        {return new Token(TokenType.STRING, yytext());}

}
<COMMENT> {
    "\n"  { yybegin(YYINITIAL); }
}

