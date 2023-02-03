
import JFlexLexer.LexicalError
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import kotlin.io.path.Path

class Etac : CliktCommand(printHelpOnEmptyArgs = true) {
    // collect input options, specify help message
    val lexFiles:List<File> by argument(help="Files to lex.", name="<source files>").file(mustExist = true).multiple()
    val print: Boolean by option("--lex",help="Generate output from lexical analysis.").flag()
    val diagnosticRelPath: String by option("-D", metavar = "<file>",
        help="Specify where to place generated diagnostic files.").default("")


    override fun run() {
        // use the input values somehow
        // paths expected to be relatives, default current working dir
        val diagnosticAbsPath = Path(System.getProperty("user.dir"),diagnosticRelPath)

        lexFiles.forEach{
            if(it.extension == "eta"){
                //We should lex the file in this case

                //Create the new lexer
                val lex = JFlexLexer(it.bufferedReader())

                //Create the new file name
                val lexedFileName = it.nameWithoutExtension + ".lexed"
                val lexedFile = File(diagnosticAbsPath.toString(),lexedFileName)

                //Create the new file if the file does not already exist
                if (print){
                    // Check if the file already exists and delete it if it does
                    if (lexedFile.exists() && !lexedFile.isDirectory){
                        lexedFile.delete()
                    }
                    lexedFile.createNewFile()
                }

                //Lex the file
                while (true) {
                    try {
                        val t: JFlexLexer.Token = lex.nextToken() ?: break
                        //Output to file if flag is set
                        if (print) {
                            lexedFile.appendText(t.toString() + "\n")
                        }

                    } catch (e: LexicalError) {
                        //Output to file if flag is set
                        if (print) {
                            lexedFile.appendText(e.lineNum.toString() + " " + e.col.toString() + " " + e.msg.toString() + "\n")
                        }
                        break
                    }
                }

            } else {
                echo("The file " + it + " is not an eta file. Skipping.")
            }
        }
    }
}

fun main(args: Array<String>) = Etac().main(args)