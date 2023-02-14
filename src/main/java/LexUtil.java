import java_cup.runtime.Symbol;

import java.math.BigInteger;
import java.util.ArrayList;

public class LexUtil {
    /** [formatChar(n)] outputs the printable version of a Character. */
    public static String formatChar(Integer character) {
        if (character == 10) return "\\n";
        if (character == 9) return "\\t";
        if (character == 13) return "\\r";
        if (character == 92) return "\\\\";
        if (character == 34) return "\\\"";
        if (character == 39) return "\\'";
        if (character < 32 || character >= 127) {
            return "\\x{" + Integer.toHexString(character) + "}";
        }
        int charTruncated = character % (1 << 16);
        char asciiChar = (char) charTruncated;
        return Character.toString(asciiChar);
    }

    /**
     * [parseToChar(matched)] converts the matched string to the integer representing
     * the character. Throws an LexicalError if the string does not correspond to a
     * character.
     */
    public static int parseToChar(String matched, int lineNum, int col) throws LexicalError {
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
                } else if (errorProne == 't'){
                    return 0x09;
                } else if  (errorProne == 'r'){
                    return 0x0D;
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
                throw new LexicalError(LexicalError.errType.UnicodeTooBig, lineNum, col);
            }
            return hexNum;
        } else {
            throw new LexicalError(LexicalError.errType.CharWrong, lineNum, col);
        }
    }

    /**
     * [parseToInt(matched)] truncates matched to fit into a long. If the number is too large, it will
     * be taken mod 2^64 and shifted to fit into the correct long range. In the specific case
     */
    public static String parseToInt(String matched, int lineNum, int col) throws LexicalError{
        BigInteger intVal = new BigInteger(matched);
        BigInteger maxVal = new BigInteger("9223372036854775808");

        if (intVal.compareTo(maxVal) <= 0) {
            return intVal.toString();
        } else {
            throw new LexicalError(LexicalError.errType.InvalidInteger, lineNum, col);
        }
    }

    public static int getSym(String attribute) {
        switch (attribute) {
            case "if": return SymbolTable.IF;
            case "return": return SymbolTable.RETURN;
            case "else": return SymbolTable.ELSE;
            case "use": return SymbolTable.USE;
            case "while": return SymbolTable.WHILE;
            case "length": return SymbolTable.LENGTH;
            case "int": return SymbolTable.INT;
            case "bool": return SymbolTable.BOOL;
            case "true": return SymbolTable.TRUE;
            case "false": return SymbolTable.FALSE;
            case "-": return SymbolTable.MINUS;
            case "!": return SymbolTable.NOT;
            case "*": return SymbolTable.TIMES;
            case "*>>": return SymbolTable.HIGHTIMES;
            case "/": return SymbolTable.DIVIDE;
            case "%": return SymbolTable.MODULO;
            case "+": return SymbolTable.PLUS;
            case "_": return SymbolTable.UNDERSCORE;
            case "<": return SymbolTable.LT;
            case "<=": return SymbolTable.LEQ;
            case ">=": return SymbolTable.GEQ;
            case ",": return SymbolTable.COMMA;
            case ">": return SymbolTable.GT;
            case "==": return SymbolTable.EQB;
            case "!=": return SymbolTable.NEQB;
            case "=": return SymbolTable.EQUALS;
            case "&": return SymbolTable.AND;
            case "|": return SymbolTable.OR;
            case "(": return SymbolTable.OPEN_PAREN;
            case ")": return SymbolTable.CLOSE_PAREN;
            case "[": return SymbolTable.OPEN_BRACKET;
            case "]": return SymbolTable.CLOSE_BRACKET;
            case "{": return SymbolTable.OPEN_BRACE;
            case "}": return SymbolTable.CLOSE_BRACE;
            case ":": return SymbolTable.COLON;
            case ";": return SymbolTable.SEMICOLON;
            default:
                return -1;
        }
    }

    static class StringTokenBuilder {
        private final int line, col;
        ArrayList<Integer> chars;

        public StringTokenBuilder(int line, int col) {
            this.line = line;
            this.col = col;
            chars = new ArrayList<>();
        }

        static String getStringRepresentation(ArrayList<Integer> list) {
            return list.stream().map(LexUtil::formatChar).collect(java.util.stream.Collectors.joining());
        }

        public void append(int lexeme) {
            chars.add(lexeme);
        }

        public int lineNumber() {
            return line;
        }

        public int column() {
            return col;
        }

        public Token.StringToken complete() {
            return new Token.StringToken(getStringRepresentation(chars), line, col);
        }
    }
}
