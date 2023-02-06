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

    /** global character array consisting of characters to be read in for a string */
    java.util.ArrayList<Character> charBuffer;

    /** [getStringRepresentation(list)] returns the string representation of an ArrayList of characters
    * from https://stackoverflow.com/questions/6324826/converting-arraylist-of-characters-to-a-string */
    String getStringRepresentation(java.util.ArrayList<Character> list)
    {
        StringBuilder builder = new StringBuilder(list.size());
        for(Character ch: list)
        {
            builder.append(ch);
        }
        return builder.toString();
    }

    /** [parseToChar(matched)] converts the matched string to the integer representing
    * the character. Throws an LexicalError if the string does not correspond to a
    * character. */
    public char parseToChar(String matched) {
        // normal case
        if (matched.length() == 1)  {
            return matched.charAt(0);
        }
        // escaped character
        else if (matched.length() == 2) {
            char errorProne = matched.charAt(1); // maybe this is \ or ', "error-prone" escapes
            // newline case
            if (errorProne == 'n')  {
                return '\n';
            }
            else { // extract the character
                return errorProne;
            }
        }
        // unicode case
        else {
        // has format "\x{<stuff>}"
            int hexNum = Integer.parseInt(matched.substring(3, matched.length() - 1), 16);
            return (char) hexNum;
        }
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
            col = column() - lex.length(); // TODO: the column probably needs to change, could be off by 1
            attribute = lex;
        }
//        /** [parseToStr(matched)] removes the end quote matched by the lexer, and cleans up
//        * any unicode characters. */ // TODO: the unicode replacement can definitely be done more cleanly
//        public String parseToStr(String matched) {
//            String ret = matched.substring(0, matched.length() - 1);
//            while (ret.contains("\\x{")) {
//                int unicodeIdx = ret.indexOf("\\x{");
//                int endUnicode = ret.indexOf("}", unicodeIdx);
//                int codePoint = Integer.parseInt(ret.substring(unicodeIdx + 3, endUnicode), 16);
//                if (isPrintable(codePoint)) {
//                    ret = ret.substring(0, unicodeIdx) + (char) (codePoint) + ret.substring(endUnicode + 1);
//                }
//            }
//            return ret;
//        }
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
            attribute = parseToChar(lex.substring(1, lex.length() - 1));
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
Unicode = "\\x{"({Digit}|[a-f]|[A-F]){1,6}"}"
Identifier = {Letter}({Digit}|{Letter}|_|')*
Integer = "0"|[1-9]{Digit}*
Character = ([^"\\"]|"\\"("\\"|"\""|"'"|"n")|{Unicode})
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
    "\""        { charBuffer = new java.util.ArrayList<Character>(); yybegin(STRING); }
    "//"         { yybegin(COMMENT); }
    // "'"           { throw new LexicalError(LexErrType.UnexpectedChar);}
}
<COMMENT> {
    "\n"  { yybegin(YYINITIAL); }
      [^] { }
}
<STRING> {
    "\""               { Token t = new StringToken(getStringRepresentation(charBuffer));
                            yybegin(YYINITIAL); return t; }
    ({Character}|"'")  { char c = parseToChar(yytext()); charBuffer.add(c); }
    [^] {  } // error state
}

[^] {  } // end of file?
