import java.util.ArrayList;
import java.math.BigInteger;
import errors.*;

%%

%public
%class JFlexLexer
%type Token
%function nextToken
%line
%column
%yylexthrow LexicalError

%unicode
%implements java_cup.runtime.Scanner
%function next_token
%type java_cup.runtime.Symbol

%eofval{
   return new java_cup.runtime.Symbol(SymbolTable.EOF);
%eofval}

%pack

%{
    private LexUtil.StringTokenBuilder currentString;

    /** Returns the line number the lexer head is currently at in the file, numbered from 1. */
    public int lineNumber() {
        return yyline + 1;
    }

    /** Returns the column the lexer head is currently at in the file, numbered from 1. */
    public int column() {
        return yycolumn + 1;
    }
%}

Whitespace = [ \t\f\r\n]
Letter = [a-zA-Z]
Digit = [0-9]
Unicode = "\\x{"({Digit}|[a-f]|[A-F]){1,6}"}"
Identifier = {Letter}({Digit}|{Letter}|_|')*
Integer = "0"|[1-9]{Digit}*
Character = [^"\\""'"]|"\\"("\\"|"\""|"'"|"n"|"t"|"r")|{Unicode}
Symbol = "-"|"!"|"*"|"*>>"|"/"|"%"|"+"|"_"|"<"|"<="|">="|","|">"|"=="|"!="|"="|"&"|"|"|"("|")"|"["|"]"|"{"|"}"|":"|";"
Reserved = "if"|"return"|"else"|"use"|"while"|"length"|"int"|"bool"|"true"|"false"
CharLiteral = "'"({Character}|"\"")"'"
MinInteger = "-"([ \t])*"9223372036854775808"

%state COMMENT
%state STRING

%%

<YYINITIAL> {
    {Whitespace}      { /* ignore */ }
    {Reserved}        { return new Token.KeywordToken(yytext(), lineNumber(), column()); }
    {Identifier}      { return new Token.IdToken(yytext(), lineNumber(), column()); }
    {MinInteger}      { return new Token.IntegerToken(yytext(), lineNumber(), column());}
    {Symbol}          { return new Token.SymbolToken(yytext(), lineNumber(), column()); }
    {Integer}         { return new Token.IntegerToken(yytext(), lineNumber(), column()); }
    {CharLiteral}     { yycolumn -= LexUtil.unicodeAdjustment(yytext().substring(1, yytext().length() - 1)); return new Token.CharacterToken(yytext(), lineNumber(), column()); }
    "\""              { currentString = new LexUtil.StringTokenBuilder(lineNumber(), column()); yybegin(STRING); }
    "//"              { yybegin(COMMENT); }
    "'"([^"\n"])      { throw new LexicalError(LexicalError.errType.CharWrong, lineNumber(), column());}
    "'"               { throw new LexicalError(LexicalError.errType.CharNotEnd, lineNumber(), column());}
    <<EOF>>           { return new Token.EOFToken(lineNumber(), column()); }
    [^]               { throw new LexicalError(LexicalError.errType.InvalidId, lineNumber(), column());}
}
<COMMENT> {
    "\n"              { yybegin(YYINITIAL); }
    <<EOF>>           { return new Token.EOFToken(lineNumber(), column()); }
    [^]               { }
}
<STRING> {
    "\""              { Token.StringToken t = currentString.complete(); yybegin(YYINITIAL); return t;}
    "\n"              { throw new LexicalError(LexicalError.errType.BadString, currentString.lineNumber(), currentString.column()); }
    ({Character}|"'") { yycolumn -= LexUtil.unicodeAdjustment(yytext()); currentString.append(LexUtil.parseToChar(yytext(), currentString.lineNumber(), currentString.column())); }
    "\\"([^])         { throw new LexicalError(LexicalError.errType.CharWrong, currentString.lineNumber(), currentString.column()); }
    <<EOF>>           { throw new LexicalError(LexicalError.errType.BadString, currentString.lineNumber(), currentString.column()); }
    [^]               { throw new LexicalError(LexicalError.errType.BadString, currentString.lineNumber(), currentString.column()); }
}

[^] {  } // end of file?