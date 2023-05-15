import Settings.Opt
import Settings.OutputIR
import assembly.AssemblyGenerator
import ast.*
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter
import errors.*
import ir.IRTranslator
import java_cup.runtime.Symbol
import typechecker.EtaType.ContextType.*
import typechecker.EtaType.OrdinaryType.*
import typechecker.TypeChecker
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * [etac] provides the central command line functionality for the compiler. It is constructed from the program arguments
 * and then dispatches commands based on that initialization.
 */
class Etac(val disableOutput: Boolean = false) : CliktCommand(printHelpOnEmptyArgs = true) {
    // collect input options, specify help message
    private val files: List<File> by argument(
        help = "Input files to compiler.", name = "<source files>"
    ).file(canBeDir = false).multiple()

    //OPTIMIZATIONS
    //TODO: actually use values
    private val disableOpt: Boolean by option(
        "-O",
        help = "Prevents optimizations (e.g. constant folding) from happening."
    ).flag()
    private val oreg: Boolean by option("-Oreg", help = "Enable register allocation and move coalescing.").flag()
    private val odce: Boolean by option("-Odce", help = "Enable dead code elimination.").flag()
    private val ocopy: Boolean by option("-Ocopy", help = "Enable copy propagation.").flag()

    //LOGISTICS
    private val outputLex: Boolean by option("--lex", help = "Generate output from lexical analysis.").flag()
    private val outputParse: Boolean by option("--parse", help = "Generate output from parser").flag()
    private val outputTyping: Boolean by option("--typecheck", help = "Generate output from typechecking").flag()
    private val initOutputIR: Boolean by option(
        "--irgen",
        help = "Generate intermediate representation as SExpr"
    ).flag()
    private val runIR: Boolean by option(
        "--irrun",
        help = "Generate and interpret intermediate representation (not fully supported)"
    ).flag()
    private val dOpt = option(
        "-D",
        metavar = "<folder>",
        help = "Specify where to place generated diagnostic files. " + "Default is the current working directory. The directory is expected to exist."
    ).default(System.getProperty("user.dir"))
    private val diagnosticRelPath: String by dOpt
    private val assemOpt = option(
        "-d",
        metavar = "<folder>",
        help = "Specify where to place generated assembly files. " + "Default is the current working directory. The directory is expected to exist."
    ).default(System.getProperty("user.dir"))
    private val assemblyRelPath: String by assemOpt
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
    private val targetOpt = option(
        "-target",
        metavar = "<OS>",
        help = "Specify the operating system for which to generate code " + "Default is linux. No other OS is supported."
    ).default("linux")
    private val target: String by targetOpt
    private val printIROpts: List<String> by option(
        "--optir", metavar = "<phase>",
        help = "Report the intermediate code at the specified phase of optimization. Supports \"initial\" and \"final\"."
    ).multiple()
    private val printCFGOpts: List<String> by option(
        "--optcfg",
        metavar = "<phase>",
        help = "Report the control-flow graph at the specified phase of optimization. Supports \"initial\" and \"final\"."
    ).multiple()

    /** [run] is the main loop of the CLI. All program arguments have already been preprocessed into vars above. */
    override fun run() {
        // the irrun flag should also generate the IR, just like irgen
        val outputIR = if (runIR) true else initOutputIR
        if (target != "linux") {
            throw BadParameterValue("The only supported OS is linux", targetOpt)
        }
        val absDiagnosticPath = processDirPath(diagnosticRelPath, dOpt)
        val absSourcepath = processDirPath(sourcepath, sourceOpt)
        val absLibpath = processDirPath(libpath, libOpt)
        val absAssemPath = processDirPath(assemblyRelPath, assemOpt)

        val expandedFiles = files.map {
            File(expandPath(it.path).toString())
        }

        val folderFiles = expandedFiles.map {
            if (it.isAbsolute) it else File(absSourcepath.toString(), it.path)
        }

        folderFiles.forEach {
            val kompiler = Kompiler()
            //the only files accepts must exist at sourcepath & be eta/eti files
            if (it.exists() && (it.extension == "eta" || it.extension == "eti" || it.extension == "rh" || it.extension == "ri")) {
                // TODO: pull out it, absDisgnosticPath
                val lexedFile: File? =
                    if (outputLex && !disableOutput) getOutFileName(it, absDiagnosticPath, ".lexed") else null
                val parsedFile: File? =
                    if (outputParse && !disableOutput) getOutFileName(it, absDiagnosticPath, ".parsed") else null
                val typedFile: File? =
                    if (outputTyping && !disableOutput) getOutFileName(it, absDiagnosticPath, ".typed") else null
                val irFile: File? =
                    if (outputIR && !disableOutput) getOutFileName(it, absDiagnosticPath, ".ir") else null
                // adding assembly file -- might be unsafe LOL
                // TODO: test output assembly file to new -d path
                val assemblyFile: File? = if (!disableOutput) getOutFileName(it, absAssemPath, ".s") else null

                val optIRInitialFile =
                    if (printIROpts.contains("initial")) getOutFileName(it, absDiagnosticPath, "_initial.ir") else null
                val optIRFinalFile =
                    if (printIROpts.contains("final")) getOutFileName(it, absDiagnosticPath, "_final.ir") else null
                val optCFGInitialFile: File? =
                    if (printCFGOpts.contains("initial")) getOutFileName(it, absDiagnosticPath, ".ignored") else null
//                print(it)
                // TODO: output path for these three pending response to my Ed post since seems weird

                val ast: Node?
                try {
                    lex(it, lexedFile)
                    try {
                        ast = parse(it, parsedFile)
                        try {
                            val context = typeCheck(it, ast, typedFile, absLibpath.toString(), kompiler)

                            when (ast) {
                                is Program -> {
                                    val translator = IRTranslator(
                                        ast,
                                        it.nameWithoutExtension,
                                        context
                                    )
                                    val ir =
                                        translator.irgen(
                                            if (disableOpt) Opt.None else Opt.All,
                                            OutputIR(optIRInitialFile, optIRFinalFile),
                                            Settings.OutputCFG(optCFGInitialFile, null)
                                        )
                                    val irFileGen = ir.java
                                    irFile?.let {
                                        val writer = CodeWriterSExpPrinter(PrintWriter(irFile))
                                        irFileGen.printSExp(writer)
                                        writer.flush()
                                        writer.close()
                                    }

                                    try {
                                        val funcMap = context.runtimeFunctionMap(translator::mangleMethodName)
                                        val assemblyAssembler = AssemblyGenerator(ir, funcMap)
                                        // print to file.s
                                        val assembly = assemblyAssembler.generate()
                                        assemblyFile?.writeText(assembly)
                                    } catch (e: Throwable) {
                                        assemblyFile?.writeText("Failed to generate assembly for " + it.name)
                                        if (!disableOutput) println("Failed to generate assembly for " + it.name)
                                    }

                                }

                                is Interface -> {
                                    irFile?.appendText("Cannot generate IR for an eti file.")
                                }

                                else -> {}
                            }
                            // we are sticking with the class IR rep, and do not implement irrun
                            if (runIR) throw ProgramResult(2)
                        } catch (e: CompilerError) {
                            if (!disableOutput) println(e.log)
                        }
                    } catch (e: ParseError) {
                        if (!disableOutput) println(e.log)
                        parsedFile?.appendText(e.mini)
                        typedFile?.appendText(e.mini)
                    }
                } catch (e: LexicalError) {
                    if (!disableOutput) println(e.log)
                    parsedFile?.appendText(e.mini)
                    typedFile?.appendText(e.mini)
                }
            } else {
                echo("Skipping $it due to invalid file.")
            }
        }
    }

    /** Takes a path string and expands beginning home reference ~ along with any instances of . and .. */
    private fun expandPath(inPath: String): Path {
        return Path(inPath.replaceFirst("~", System.getProperty("user.home"))).normalize()
    }

    /**
     * Expand and make absolute a possibly relative directory path. Validate the directory existence.
     *
     * @throws BadParameterValue when the directory is invalid
     */
    private fun processDirPath(inPath: String, option: OptionWithValues<String, String, String>): Path {
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

    private fun getOutFileName(inFile: File, diagnosticPath: Path, extension: String): File {
        val outFileName = inFile.nameWithoutExtension + extension
        val outFile = File(diagnosticPath.toString(), outFileName)
        if (outFile.exists() && !outFile.isDirectory) {
            outFile.delete()
        }
        outFile.createNewFile()
        return outFile
    }

    @Throws(LexicalError::class)
    private fun lex(inFile: File, lexedFile: File?) {
        val jFlexLexer = JFlexLexer(inFile.bufferedReader(), inFile, inFile.extension)
        while (true) {
            try {
                val t: Symbol = (jFlexLexer.next_token() ?: break)
                if (t.sym == SymbolTable.EOF) break
                lexedFile?.appendText((t as Token<*>).lexInfo() + "\n")
            } catch (e: LexicalError) {
                lexedFile?.appendText("${e.mini}\n")
                throw e
            }
        }
    }

    @Throws(ParseError::class)
    private fun parse(inFile: File, parsedFile: File?): Node {
        val AST = ASTUtil.getAST(inFile.absoluteFile)
        parsedFile?.let {
            val writer = CodeWriterSExpPrinter(PrintWriter(parsedFile))
            AST.write(writer)
            writer.flush()
            writer.close()
        }
        return AST
    }

    @Throws(SemanticError::class)
    private fun typeCheck(
        inFile: File,
        ast: Node,
        typedFile: File?,
        libpath: String,
        kompiler: Kompiler
    ): typechecker.Context {
        try {
            val topGamma = kompiler.createTopLevelContext(inFile, ast, libpath, typedFile)
            var tc = TypeChecker(topGamma, inFile)
            if (ast !is Interface) {
                tc.typeCheck(ast)
            }
            typedFile?.appendText("Valid Eta Program")
            return tc.Gamma
        } catch (e: CompilerError) {
            // only append if error in import has not already been appended inside cTLC
            if (e.file == inFile) typedFile?.appendText(e.mini)
            throw e
        }
    }

}

fun main(args: Array<String>) = Etac().main(args)
