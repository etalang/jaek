import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlin.io.path.Path

class Etac : CliktCommand(printHelpOnEmptyArgs = true) {
    // collect input options, specify help message
    val print: Boolean by option("--lex",help="Generate output from lexical analysis.").flag()
    val diagnosticRelPath: String by option("-D", metavar = "<file>",
        help="Specify where to place generated diagnostic files.").default("")

    override fun run() {
        // use the input values somehow
        // paths expected to be relatives, default current working dir
        val diagnosticAbsPath = Path(System.getProperty("user.dir"),diagnosticRelPath)
        print(diagnosticAbsPath)
    }
}

fun main(args: Array<String>) = Etac().main(args)