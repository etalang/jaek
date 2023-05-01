import UltimateLexer.HeaderToken
import ast.Node
import errors.ParseError
import java.io.File

class ASTUtil {

    companion object {
        @Throws(ParseError::class)
        fun getAST(file : File) : Node {
            val fileType: HeaderToken? = when (file.extension) {
                "eta", "rh" -> HeaderToken.PROGRAM
                "eti", "ri" -> HeaderToken.INTERFACE
                else -> null
            }
            val lexer = UltimateLexer(file.bufferedReader(), fileType, file)
            @Suppress("DEPRECATION") val parser = parser(lexer)
            parser.setFile(file)
            parser.setExtension(file.extension)
            return parser.parse().value as Node
        }
    }
}