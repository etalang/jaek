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
        help = "Specify where to place generated diagnostic files. " +
                "Default is the current working directory. The directory is expected to exist."
    ).default(System.getProperty("user.dir"))
    private val diagnosticRelPath: String by dOpt
    private val sourceOpt = option(
        "-sourcepath",
        metavar = "<folder>",
        help = "Specify where to locate input files. " +
                "Default is the current working directory. The directory is expected to exist."
    ).default(System.getProperty("user.dir"))
    private val sourcepath: String by sourceOpt

    /**
     * [run] is the main loop of the CLI. All program arguments have already been
     * preprocessed into vars above.
     */
    override fun run() {
        if (!File(diagnosticRelPath).isDirectory) { //output dir must be dir
            throw BadParameterValue(text = "The file output location must be an existing directory.", option = dOpt)
        }

        if (!File(sourcepath).isDirectory) { //input dir must be dir
            throw BadParameterValue(text = "The file input location must be an existing directory.", option = sourceOpt)
        }

        val diagnosticPath = if (Path(diagnosticRelPath).isAbsolute) {
            Path(diagnosticRelPath)
        } else { // create absolute path from current dir and relative path
            Path(System.getProperty("user.dir"), diagnosticRelPath)
        }
        val absSourcepath = if (Path(sourcepath).isAbsolute) {
            Path(sourcepath)
        } else { // create absolute path from current dir and relative path
            Path(System.getProperty("user.dir"), sourcepath)
        }

        val folderFiles = files.map { File(absSourcepath.toString(), it.path) }
        folderFiles.forEach {
            //the only files accepts must exist at sourcepath, be eta/eti files
            if (it.exists() && (it.extension == "eta" || it.extension == "eti")) {
                if (print_lex) {
                    //"double-lex" to guarantee lexing completion even if parse fails
                    val lexedFileName = it.nameWithoutExtension + ".lexed"
                    val lexedFile = File(diagnosticPath.toString(), lexedFileName)
                    if (lexedFile.exists() && !lexedFile.isDirectory) {
                        lexedFile.delete()
                    }
                    lexedFile.createNewFile()
                    val lex = JFlexLexer(it.bufferedReader())
                    
                    //truly awful
                    while (true) {
                        try {
                            val t: Symbol = (lex.next_token() ?: break)
                            if (t.sym == SymbolTable.EOF) break
                            lexedFile.appendText((t as Token<*>).lexInfo() + "\n")
                        } catch (e: LexicalError) {
                            lexedFile.appendText("${e.msg}\n")
                            break
                        }
                    }
                }
                
                val fileType: HeaderToken? = when (it.extension) {
                    "eta" -> HeaderToken.PROGRAM;
                    "eti" -> HeaderToken.INTERFACE;
                    else -> null;
                }
                val lexer = UltimateLexer(it.bufferedReader(), fileType)
                val parser = parser(lexer)
                val out = parser.parse()
                if (print_parse) print(out.value) // will need to cast this according to example
            } else {
                echo("Skipping $it due to invalid file.")
            }
        }
    }
}

fun main(args: Array<String>) = Etac().main(args)
