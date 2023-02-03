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

    /** A Token consists of the corresponding string lexeme [lexeme], positioning information
     *  ([lineNum], [col]), and if applicable, the literal value [attribute]. The attribute should be
     *  as accurate as possible to the semantic meaning of the string. */
    abstract class Token {
        String lexeme;
        int lineNum;
        int col;
        Token(String lex) {
            lineNum = lineNumber(); col = column(); lexeme = lex;
        }
        public String toString() {
            return "" + lineNum + ":" + col + " " + lexeme;
        }
    }

    class StringToken extends Token {
        String attribute;
        StringToken(String lex)  {
            super(lex);
            col = column() - 1; // need this to offset missing quotation
            attribute = lex;
        }
        /** [parseToStr(matched)] removes the end quote matched by the lexer, and cleans up
        * any unicode characters. */ // TODO: doesn't actually do the unicode thing
        public String parseToStr(String matched) {
            String ret = matched.substring(0, matched.length() - 1);
            return ret;
        }
        public String toString() {
            return "" + lineNum + ":" + col + " string " + attribute;
        }
    }

    class IntegerToken extends Token {
        int attribute;
        IntegerToken(String lex)  {
            super(lex);
            attribute = Integer.parseInt(lex);
        }
        public String toString() {
            return "" + lineNum + ":" + col + " integer " + attribute;
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
                else {
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
            return "" + lineNum + ":" + col + " character " + (char) attribute;
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
            return "" + lineNum + ":" + col + " id " + lexeme;
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
