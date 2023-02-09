import java.util.ArrayList;
import java.math.BigInteger;

public class LexUtil {
    static String getStringRepresentation(ArrayList<Integer> list) {
        return list.stream().map(LexUtil::formatChar).collect(java.util.stream.Collectors.joining());
    }


    /** [formatChar(n)] outputs the printable version of a Character. */
    public static String formatChar(Integer character) {
        if (character == 10) return "\\n";
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
                throw new LexicalError(LexErrType.UnicodeTooBig, lineNum, col);
            }
            return hexNum;
        } else {
            throw new LexicalError(LexErrType.CharWrong, lineNum, col);
        }
    }

    /**
     * [parseToInt(matched)] truncates matched to fit into a long. If the number is too large, it will
     * be taken mod 2^64 and shifted to fit into the correct long range. In the specific case
     */
    public static long parseToInt(String matched) {
        if (matched.length() <= 18) { // there are 19 digits in 2^63
            return Long.parseLong(matched);
        } else {
            return new BigInteger(matched).longValue();
        }
    }
}
