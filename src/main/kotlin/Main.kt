import UltimateLexer.HeaderToken
import ast.*
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter
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
    private val outputLex: Boolean by option("--lex", help = "Generate output from lexical analysis.").flag()
    private val outputParse: Boolean by option("--parse", help = "Generate output from parser").flag()
    private val dOpt = option(
        "-D",
        metavar = "<folder>",
        help = "Specify where to place generated diagnostic files. " + "Default is the current working directory. The directory is expected to exist."
    ).default(System.getProperty("user.dir"))
    private val diagnosticRelPath: String by dOpt
    private val sourceOpt = option(
        "-sourcepath",
        metavar = "<folder>",
        help = "Specify where to locate input files. " + "Default is the current working directory. The directory is expected to exist."
    ).default(System.getProperty("user.dir"))
    private val sourcepath: String by sourceOpt

    /**
     * [run] is the main loop of the CLI. All program arguments have already been
     * preprocessed into vars above.
     */
    override fun run() {
        if (!File(diagnosticRelPath).isDirectory) throw BadParameterValue(
            text = "The file output location must be an existing directory.", option = dOpt
        )

        if (!File(sourcepath).isDirectory) throw BadParameterValue(
            text = "The file input location must be an existing directory.", option = sourceOpt
        )

        val diagnosticPath = when {
            Path(diagnosticRelPath).isAbsolute -> Path(diagnosticRelPath)
            else -> Path(System.getProperty("user.dir"), diagnosticRelPath)
        }

        val absSourcepath = when {
            (Path(sourcepath).isAbsolute) -> Path(sourcepath)
            else -> Path(System.getProperty("user.dir"), sourcepath)
        }

        val folderFiles = files.map { File(absSourcepath.toString(), it.path) }
        folderFiles.forEach {
            //the only files accepts must exist at sourcepath & be eta/eti files
            if (it.exists() && (it.extension == "eta" || it.extension == "eti") && (outputLex || outputParse)) {
                var lexError: String? = null
                var lexedFile: File? = null
                if (outputLex) {
                    //"double-lex" to guarantee lexing completion even if parse fails
                    val lexedFileName = it.nameWithoutExtension + ".lexed"
                    lexedFile = File(diagnosticPath.toString(), lexedFileName)
                    if (lexedFile.exists() && !lexedFile.isDirectory) {
                        lexedFile.delete()
                    }
                    lexedFile.createNewFile()
                }
                val jFlexLexer = JFlexLexer(it.bufferedReader())

                while (true) {
                    try {
                        val t: Symbol = (jFlexLexer.next_token() ?: break)
                        if (t.sym == SymbolTable.EOF) break
                        if (outputLex && lexedFile != null) lexedFile.appendText((t as Token<*>).lexInfo() + "\n")
                    } catch (e: LexicalError) {
                        if (outputLex && lexedFile != null) lexedFile.appendText("${e.msg}\n")
                        lexError = e.msg
                        break
                    }
                }

                if (outputParse) {
                    val fileType: HeaderToken? = when (it.extension) {
                        "eta" -> HeaderToken.PROGRAM
                        "eti" -> HeaderToken.INTERFACE
                        else -> null
                    }
                    val parsedFileName = it.nameWithoutExtension + ".parsed"
                    val parsedFile = File(diagnosticPath.toString(), parsedFileName)
                    if (parsedFile.exists() && !parsedFile.isDirectory) {
                        parsedFile.delete()
                    }
                    parsedFile.createNewFile()

                    if (lexError == null) {
                        val lexer = UltimateLexer(it.bufferedReader(), fileType)
                        @Suppress("DEPRECATION") val parser = parser(lexer)
                        try {
                            val AST = parser.parse().value
                            if (outputParse) {
                                val writer = CodeWriterSExpPrinter(PrintWriter(parsedFile))
                                ((AST as Node).write(writer))
                                writer.flush()
                                writer.close()
                            }
                        } catch (e: ParseError) {
                            val badSym = e.sym
                            if (badSym is Token<*>) {
                                val err = "${badSym.location()} error:${badSym.stringVal()}"
                                parsedFile.appendText(err)
                                println(err)
                            } else {
                                val err = "Unexpected error while parsing."
                                parsedFile.appendText(err)
                                println(err)
                            }
                        }
                    } else {
                        parsedFile.appendText(lexError)
                        println(lexError)
                    }
                }
            } else {
                echo("Skipping $it due to invalid file.")
            }
        }
    }
}

fun main(args: Array<String>) = Etac().main(args)
