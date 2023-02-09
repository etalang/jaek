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
    /** global character array consisting of characters to be read in for a string */
    

    /** Returns the line number the lexer head is currently at in the file, numbered from 1. */
    public int lineNumber() {
        return yyline + 1;
    }

    /** Returns the column the lexer head is currently at in the file, numbered from 1. */
    public int column() {
        return yycolumn + 1;
    }

    /**
     * [parseToChar(matched)] converts the matched string to the integer representing
     * the character. Throws an LexicalError if the string does not correspond to a
     * character.
     */
    public int parseToChar(String matched) throws LexicalError {
        // normal case
        if (matched.length() == 1) {
            return matched.codePointAt(0);
        } else if (matched.length() == 2) {
            if (matched.charAt(0) == '\\') {
                // escaped character
                char errorProne = matched.charAt(1); // maybe this is \ or ', "error-prone" escapes
                // newline case
                if (errorProne == 'n') {
                    return 0x0A;
                } else { // extract the character
                    return (int) errorProne;
                }
            } else {
                //character made of two characters
                return matched.codePointAt(0);
            }
        }
        // unicode case
        else if (matched.length() >= 5 && matched.startsWith("\\x{")) {
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
            return new BigInteger(matched).longValue();
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
    "'"           { throw new LexicalError(LexErrType.CharNotEnd, lineNumber(), column());}
}
<COMMENT> {
    "\n"  { yybegin(YYINITIAL); }
      [^] { }
}
<STRING> {
    "\""               { Token t = new StringToken(charBuffer);
                            yybegin(YYINITIAL); return t; }
    "\n"          { throw new LexicalError(LexErrType.MultilineString, lineNumber(), column()); }
    ({Character}|"'")  { int c = parseToChar(yytext()); charBuffer.add(c); }
    [^]                {throw new LexicalError(LexErrType.StringNotEnd, lineNumber(), column()); }
}

[^] {  } // end of file?
