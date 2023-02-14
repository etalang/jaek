import UltimateLexer.HeaderToken
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java_cup.runtime.Symbol
import java.io.File
import java.io.PrintWriter
import kotlin.io.path.Path

/**
 * [etac] provides the central command line functionality for the compiler. It is
 * constructed from the program arguments and then dispatches commands based on that
 * initialization.
 */
class Etac : CliktCommand(printHelpOnEmptyArgs = true) {
    // collect input options, specify help message
    private val files: List<File> by argument(
        help = "Input files to compiler.", name = "<source files>"
    ).file(canBeDir = false).multiple()
    private val print_lex: Boolean by option("--lex", help = "Generate output from lexical analysis.").flag()
    private val print_parse: Boolean by option("--parse", help = "Generate output from parser").flag()
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

        files.forEach {
            if (it.exists()) {
                if (print_parse) {
                    val fileType: HeaderToken? = when (it.extension) {
                        "eta" -> HeaderToken.PROGRAM;
                        "eti" -> HeaderToken.INTERFACE;
                        else -> null;
                    }

                    var output: PrintWriter? = null;
                    if (print_lex) {
                        val lexedFileName = it.nameWithoutExtension + ".lexed"
                        val lexedFile = File(diagnosticPath.toString(), lexedFileName)

                        // Check if the file already exists and delete it if it does
                        if (lexedFile.exists() && !lexedFile.isDirectory) {
                            lexedFile.delete()
                        }
                        lexedFile.createNewFile()

                        output = PrintWriter(lexedFile);
                    }
                    val lexer = UltimateLexer(it.bufferedReader(), fileType, output, print_lex)
                    val parser = parser(lexer)
                    parser.parse()
                } else {
                    if (it.extension == "eta" && it.exists()) {
                        val lex = JFlexLexer(it.bufferedReader())
                        val lexedFileName = it.nameWithoutExtension + ".lexed"
                        val lexedFile = File(diagnosticPath.toString(), lexedFileName)

                        //Create the new file if the file does not already exist
                        if (print_lex) {
                            // Check if the file already exists and delete it if it does
                            if (lexedFile.exists() && !lexedFile.isDirectory) {
                                lexedFile.delete()
                            }
                            lexedFile.createNewFile()
                        }

                        //Lex the file
                        while (true) {
                            try {
                                val t: Symbol = (lex.next_token() ?: break)
                                if (t.sym == SymbolTable.EOF) break
                                //Output to file if flag is set
                                if (print_lex) {
                                    lexedFile.appendText((t as Token<*>).lexInfo() + "\n")
                                }

                            } catch (e: LexicalError) {
                                //Output to file if flag is set
                                if (print_lex) {
                                    lexedFile.appendText("${e.msg}\n")
                                }
                                break
                            }
                        }
                    }

                }
            } else {
                echo("Skipping.")
            }
        }
    }
}

fun main(args: Array<String>) = Etac().main(args)
