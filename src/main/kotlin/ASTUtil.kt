import UltimateLexer.HeaderToken
import ast.Node
import errors.ParseError
import java.io.File

class ASTUtil {

    companion object {
        @Throws(ParseError::class)
        fun getAST(file : File) : Node {
            val fileType: HeaderToken? = when (file.extension) {
                "eta" -> HeaderToken.PROGRAM
                "eti" -> HeaderToken.INTERFACE
                else -> null
            }
            val lexer = UltimateLexer(file.bufferedReader(), fileType)
            @Suppress("DEPRECATION") val parser = parser(lexer)
            return parser.parse().value as Node
        }
    }
}