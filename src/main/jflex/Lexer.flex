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
%implements java_cup.runtime.Scanner
%function next_token
%type java_cup.runtime.Symbol

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
Character = [^"\\""'"]|"\\"("\\"|"\""|"'"|"n")|{Unicode}
Symbol = "-"|"!"|"*"|"*>>"|"/"|"%"|"+"|"_"|"<"|"<="|">="|","|">"|"=="|"!="|"="|"&"|"|"|"("|")"|"["|"]"|"{"|"}"|":"|";"
Reserved = "if"|"return"|"else"|"use"|"while"|"length"|"int"|"bool"|"true"|"false"
CharLiteral = "'"({Character}|"\"")"'"

%state COMMENT
%state STRING

%%

<YYINITIAL> {
    {Whitespace}  { /* ignore */ }
    {Reserved}     { return new KeywordToken(yytext(), lineNumber(), column()); }
    {Identifier}  { return new IdToken(yytext(), lineNumber(), column()); }
    {Symbol}    { return new SymbolToken(yytext(), lineNumber(), column()); }
    {Integer}     { return new IntegerToken(yytext(), lineNumber(), column()); }
    {CharLiteral}    { return new CharacterToken(yytext(), lineNumber(), column()); }
    "\""        { currentString = new LexUtil.StringTokenBuilder(lineNumber(), column()); yybegin(STRING); }
    "//"         { yybegin(COMMENT); }
    "'"           { throw new LexicalError(LexicalError.errType.CharNotEnd, lineNumber(), column());}
}
<COMMENT> {
    "\n"  { yybegin(YYINITIAL); }
      [^] { }
}
<STRING> {
    "\""               { StringToken t= currentString.complete(); yybegin(YYINITIAL); return t;}
    "\n"          { throw new LexicalError(LexicalError.errType.MultilineString, lineNumber(), column()); }
    ({Character}|"'")  { currentString.append(LexUtil.parseToChar(yytext(), lineNumber(), column())); }
    [^]                {throw new LexicalError(LexicalError.errType.StringNotEnd, lineNumber(), column()); }
}

[^] {  } // end of file?
