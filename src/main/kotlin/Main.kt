
import ast.*
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter
import errors.*
import java_cup.runtime.Symbol
import typechecker.TypeChecker
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * [etac] provides the central command line functionality for the compiler. It is
 * constructed from the program arguments and then dispatches commands based on that
 * initialization.
 */
class Etac : CliktCommand(printHelpOnEmptyArgs = true) {
    // collect input options, specify help message
    // TODO: accept relative paths
    private val files: List<File> by argument(
        help = "Input files to compiler.", name = "<source files>"
    ).file(canBeDir = false).multiple()
    private val outputLex: Boolean by option("--lex", help = "Generate output from lexical analysis.").flag()
    private val outputParse: Boolean by option("--parse", help = "Generate output from parser").flag()
    private val outputTyping: Boolean by option("--typecheck", help = "Generate output from typechecking").flag()
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
    private val libOpt = option(
        "-libpath",
        metavar = "<folder>",
        help = "Specify where to find library or interface files. " + "Default is the current working directory. The directory is expected to exist."
    ).default(System.getProperty("user.dir"))
    private val libpath: String by libOpt
    data class CurrFile(var file : File)
    /**
     * [run] is the main loop of the CLI. All program arguments have already been
     * preprocessed into vars above.
     */
    override fun run() {

        val absDiagnosticPath = processDirPath(diagnosticRelPath, dOpt)
        val absSourcepath = processDirPath(sourcepath, sourceOpt)
        val absLibpath = processDirPath(libpath, libOpt)

        val expandedFiles = files.map{
            File(expandPath(it.path).toString())
        }

        val folderFiles = expandedFiles.map {
            if (it.isAbsolute) it else File(absSourcepath.toString(), it.path)
        }

        folderFiles.forEach {
            val currFile = CurrFile(it) //holds the currently processing file for error reporting
            val kompiler = Kompiler()
            //the only files accepts must exist at sourcepath & be eta/eti files
            if (it.exists() && (it.extension == "eta" || it.extension == "eti")) {
                val lexedFile: File? = if (outputLex) getOutFileName(it, absDiagnosticPath, ".lexed") else null
                val parsedFile: File? = if (outputParse) getOutFileName(it, absDiagnosticPath, ".parsed") else null
                val typedFile: File? = if (outputTyping) getOutFileName(it, absDiagnosticPath, ".typed") else null
                try {
                    lex(it, lexedFile)
                    val ast = parse(it, parsedFile)
                    typeCheck(ast, typedFile, absLibpath.toString(), currFile, kompiler)
                } catch (e : CompilerError) {
                    when (e) {
                        is LexicalError -> {
                            println("Lexical error beginning at ${currFile.file.name}:${e.line}:${e.column}: ${e.details()}")
                            //lexical error goes in remaining out files, do not pass GO
                            parsedFile?.appendText(e.msg)
                            typedFile?.appendText(e.msg)
                        }
                        is ParseError -> {
                            val badSym = e.sym
                            var err = ""
                            if (badSym is Token<*>) {
                                err = "${badSym.location()} error:${badSym.stringVal()}"
                                println("Syntax error beginning at ${currFile.file.name}:${badSym.line}:${badSym.col}: ${badSym.stringVal()}")
                            } else {
                                err = "Unexpected error while parsing."
                            }
                            parsedFile?.appendText(err)
                            typedFile?.appendText(err)

                        }
                        is SemanticError -> {
                            println("Semantic error beginning at ${currFile.file.name}:${e.line}:${e.column}: ${e.desc}")
                        }
                    }

                }
            } else {
                echo("Skipping $it due to invalid file.")
            }
        }
    }

    /**
     * Takes a path string and expands beginning home reference ~ along with any instances of . and ..
     */
    private fun expandPath(inPath : String) : Path {
        return Path(inPath.replaceFirst("~", System.getProperty("user.home"))).normalize()
    }

    /**
     * Expand and make absolute a possibly relative directory path. Validate the directory existence.
     * @throws BadParameterValue when the directory is invalid
     */
    private fun processDirPath(inPath : String, option : OptionWithValues<String,String,String>) : Path {
        val expandedInPath = expandPath(inPath)

        val absInPath = when {
            (expandedInPath.isAbsolute) -> expandedInPath
            else -> Path(System.getProperty("user.dir"), expandedInPath.toString())
        }

        if (!File(absInPath.toString()).isDirectory) throw BadParameterValue(
            text = "The file location must be an existing directory.", option = option
        )
        return absInPath
    }
    private fun getOutFileName(inFile: File, diagnosticPath: Path, extension: String) : File {
        val outFileName = inFile.nameWithoutExtension + extension
        val outFile = File(diagnosticPath.toString(), outFileName)
        if (outFile.exists() && !outFile.isDirectory) {
            outFile.delete()
        }
        outFile.createNewFile()
        return outFile
    }
    private fun lex(inFile: File, lexedFile : File?) {
        val jFlexLexer = JFlexLexer(inFile.bufferedReader())
        while (true) {
            try {
                val t: Symbol = (jFlexLexer.next_token() ?: break)
                if (t.sym == SymbolTable.EOF) break
                lexedFile?.appendText((t as Token<*>).lexInfo() + "\n")
            } catch (e: LexicalError) {
                lexedFile?.appendText("${e.msg}\n")
                throw e // should throw uncaught error to main pipeline
            }
        }
    }

    private fun parse(inFile: File, parsedFile : File?) : Node {
        val AST = ASTUtil.getAST(inFile.absoluteFile)
        if (parsedFile != null) {
            val writer = CodeWriterSExpPrinter(PrintWriter(parsedFile))
            AST.write(writer)
            writer.flush()
            writer.close()
        }
        return AST
    }

    private fun typeCheck(
        ast: Node,
        typedFile: File?,
        libpath: String,
        currFile: CurrFile,
        kompiler: Kompiler
    ) {
        try {
            val topGamma = kompiler.createTopLevelContext(ast, libpath, typedFile, currFile)
            if (ast !is Interface) {
                TypeChecker(topGamma).typeCheck(ast)
            }
            typedFile?.appendText("Valid Eta Program")
        } catch (e : SemanticError) {
            if (typedFile != null && typedFile.length() == 0L) typedFile?.appendText("${e.line}:${e.column} error:${e.desc}")
            throw e
        }
    }

}
fun main(args: Array<String>) = Etac().main(args)
