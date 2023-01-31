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
        /* reserved keywords */
        ELSE, IF, LENGTH, RETURN, WHILE, USE,
        /* names of datatypes */
        BOOL, INT,
        /* data types */
        INTEGER, STRING, CHARACTER, BOOLEAN,
        /* grouping */
        LBRACE, LPAREN, RBRACE, RPAREN,
        /* syntactic characters */
        COLON, SEMICOLON, LBRACKET, RBRACKET,
        /* operators */ // TODO: come up with better names for some of these
        PLUS, MINUS, STAR, STARGG, SLASH, BANG, PERCENT, LESS, LEQ,
        GREATER, GEQ, EEQUALS, NEQUALS, AND, OR, EQUALS,
        /* identifier */
        ID
    }

    /** A Token consists of a TokenType [type], positioning information
    * ([lineNum], [col]), and potentially a literal represented by [attribute]. */
    class Token {
        TokenType type;
        int lineNum;
        int col;
        Object attribute;
        Token(TokenType tt) {
            type = tt; attribute = null; lineNum = lineNumber(); col = column();
        }
        Token(TokenType tt, Object attr) {
            type = tt; attribute = attr; lineNum = lineNumber(); col = column();
        }
        public String toString() {
            String tokenInfo = "" + lineNum + ":" + col + " " + TTtoString(type);
            if (attribute != null)
                return tokenInfo + " " + attribute;
            else
                return tokenInfo;
        }
    }
    /** Returns the line number the lexer head is currently at
     *  in the file, numbered from 1. */
    public int lineNumber() { return yyline + 1; }

    /** Returns the column the lexer head is currently at
     *  in the file, numbered from 1. */
    public int column() { return yycolumn + 1; }

    // TODO: this is disgusting
    /** [TTtoString(tt)] converts a TokenType [tt] to a string
    * consistent with the test output. */
    public String TTtoString(TokenType tt) {
        switch (tt) {
            /* reserved keywords */
            case ELSE: return "else"; case IF: return "if";
            case LENGTH: return "length"; case RETURN: return "return";
            case WHILE: return "while"; case USE: return "use";
            /* names of datatypes */
            case BOOL: return "bool"; case INT: return "int";
            /* data types */
            case INTEGER: return "integer"; case STRING: "string";
            case CHARACTER: return "character"; case BOOLEAN: return "boolean";
            /* grouping */
            case LBRACE: return "{"; case LPAREN: return "(";
            case RBRACE: return "}"; case RPAREN: return ")";
            /* syntactic characters */
            case COLON: return ":"; case SEMICOLON: ";";
            case LBRACKET: "["; case RBRACKET: "]";
            /* operators */
            case PLUS: return "+"; case MINUS: return "-";
            case STAR: return "*"; case STARGG: return "*>>";
            case SLASH: return "/"; case BANG: return "!";
            case PERCENT: return "%"; case LESS: return "<";
            case LEQ: return "<="; case GREATER: return ">";
            case GEQ: return ">="; case EEQUALS: return "==";
            case NEQUALS: return "!="; case AND: return "&";
            case OR: return "|"; case EQUALS: return "=";
            /* identifier */
            case ID: return "id";
            default: return "L bozo"; // TODO: should raise exception
        }
    }
%}

Whitespace = [ \t\f\r\n]
Letter = [a-zA-Z]
Digit = [0-9]
Identifier = {Letter}({Digit}|{Letter}|_|')*
Integer = "0"|"-"?[1-9]{Digit}*

%state COMMENT
%state STRING

%%

<YYINITIAL> {
    {Whitespace}  { /* ignore */ }
    "if"          { return new Token(TokenType.IF); }
    {Identifier}  { return new Token(TokenType.ID, yytext()); }
    {Integer}     { return new Token(TokenType.INT,
    				 Integer.parseInt(yytext())); }
    "."           { return new Token(TokenType.DOT); }
}
<COMMENT> {
    "\n"  { yybegin(YYINITIAL); }
}
<STRING> {
    "\""  { yybegin(YYINITIAL); }
}
