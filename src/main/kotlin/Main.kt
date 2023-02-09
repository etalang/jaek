import JFlexLexer.LexicalError
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import kotlin.io.path.Path

/**
 * [etac] provides the central command line functionality for the compiler. It is
 * constructed from the program arguments and then dispatches commands based on that
 * initialization.
 */
class Etac : CliktCommand(printHelpOnEmptyArgs = true) {
    // collect input options, specify help message
    private val lexFiles: List<File> by argument(help = "Files to lex.", name = "<source files>").file(canBeDir = false)
        .multiple()
    private val print: Boolean by option("--lex", help = "Generate output from lexical analysis.").flag()
    private val dOpt = option(
        "-D",
        metavar = "<folder>",
        help = "Specify where to place generated diagnostic files. " + "Default is the current working directory. The directory is expected to exist."
    ).default(System.getProperty("user.dir"))
    private val diagnosticRelPath: String by dOpt

    /**
     * [run] is the main loop of the CLI. All program arguments have already been
     * preprocessed into vars above.
     */
    override fun run() {
        if (!File(diagnosticRelPath).isDirectory) { //output dir must be dir
            throw BadParameterValue(text = "The file output location must be an existing directory.", option = dOpt)
        }

        val diagnosticPath = if (Path(diagnosticRelPath).isAbsolute) {
            Path(diagnosticRelPath)
        } else { // create absolute path from current dir and relative path
            Path(System.getProperty("user.dir"), diagnosticRelPath)
        }

        lexFiles.forEach {
            if (it.extension == "eta" && it.exists()) {
                //We should lex the file in this case

                //Create the new lexer
                val lex = JFlexLexer(it.bufferedReader())

                val parser = parser(lex);

                //Create the new file name
                val lexedFileName = it.nameWithoutExtension + ".lexed"
                val lexedFile = File(diagnosticPath.toString(), lexedFileName)

                //Create the new file if the file does not already exist
                if (print) {
                    // Check if the file already exists and delete it if it does
                    if (lexedFile.exists() && !lexedFile.isDirectory) {
                        lexedFile.delete()
                    }
                    lexedFile.createNewFile()
                }

                //Lex the file
                while (true) {
                    try {
                        val t : JFlexLexer.Token = (lex.next_token() ?: break) as JFlexLexer.Token
                        //Output to file if flag is set
                        if (print) {
                            lexedFile.appendText(t.toString() + "\n")
                        }

                    } catch (e: LexicalError) {
                        //Output to file if flag is set
                        if (print) {
                            lexedFile.appendText(e.lineNum.toString() + ":" + e.col.toString() + " error:" + e.msg.toString() + "\n")
                        }
                        break
                    }
                }

            } else {
                echo("The file $it is not an eta file or does not exist. Skipping.")
            }
        }
    }
}

fun main(args: Array<String>) = Etac().main(args)
