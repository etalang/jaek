import java.math.BigInteger
import java.util.stream.Collectors

object LexUtil {
    @JvmStatic
    fun getStringRepresentation(list: ArrayList<Int>): String {
        return list.stream().map { obj: Int -> formatChar(obj) }.collect(Collectors.joining())
    }

    /** [formatChar(n)] outputs the printable version of a Character.  */
    @JvmStatic
    fun formatChar(character: Int): String {
        if (character == 10) return "\\n"
        if (character < 32 || character >= 127) {
            return "\\x{" + Integer.toHexString(character) + "}"
        }
        val charTruncated = character % (1 shl 16)
        val asciiChar = charTruncated.toChar()
        return Character.toString(asciiChar)
    }

    /**
     * [parseToChar(matched)] converts the matched string to the integer representing
     * the character. Throws an LexicalError if the string does not correspond to a
     * character.
     */
    @JvmStatic
    @Throws(LexicalError::class)
    fun parseToChar(matched: String, lineNum: Int, col: Int): Int {
        // normal case
        return if (matched.length == 1) {
            matched.codePointAt(0)
        } else if (matched.length == 2) {
            if (matched[0] == '\\') {
                // escaped character
                val errorProne = matched[1] // maybe this is \ or ', "error-prone" escapes
                // newline case
                if (errorProne == 'n') {
                    0x0A
                } else { // extract the character
                    errorProne.code
                }
            } else {
                //character made of two characters
                matched.codePointAt(0)
            }
        } else if (matched.length >= 5 && matched.startsWith("\\x{")) {
            // has format "\x{<stuff>}"
            val hexNum = matched.substring(3, matched.length - 1).toInt(16)
            if (hexNum < 0 || hexNum >= 1 shl 24) {
                throw LexicalError(LexErrType.UnicodeTooBig, lineNum, col)
            }
            hexNum
        } else {
            throw LexicalError(LexErrType.CharWrong, lineNum, col)
        }
    }

    /**
     * [parseToInt(matched)] truncates matched to fit into a long. If the number is too large, it will
     * be taken mod 2^64 and shifted to fit into the correct long range. In the specific case
     */
    @JvmStatic
    fun parseToInt(matched: String): Long {
        return if (matched.length <= 18) { // there are 19 digits in 2^63
            matched.toLong()
        } else {
            BigInteger(matched).toLong()
        }
    }
}