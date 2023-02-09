import java.util.ArrayList;

public class LexUtil {
    String getStringRepresentation(ArrayList<Integer> list) {
        return list.stream().map(LexUtil::formatChar).collect(java.util.stream.Collectors.joining());
    }


    /** [formatChar(n)] outputs the printable version of a Character. */
    private static String formatChar(Integer character) {
        if (character == 10) return "\\n";
        if (character < 32 || character >= 127) {
            return "\\x{" + Integer.toHexString(character) + "}";
        }
        int charTruncated = character % (1 << 16);
        char asciiChar = (char) charTruncated;
        return Character.toString(asciiChar);
    }
}
