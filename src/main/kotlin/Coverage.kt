import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.math.max

/**
 *  Some of the worst code you can find in this repository.
 *  This code relies on absurd, arbitrary style choices of how we format our ethScript file.
 *  This should not be used for anything besides quickly generating a coverage report when
 *  the project is due within 24 hours. Thank you.
 */
fun main() {
    val reader = BufferedReader(FileReader(File("src/tests/ethScript")))
    var directory = ""
    reader.useLines { line ->
        line.forEach {
            if (it.startsWith("etac")) {
                directory = it.substring(it.indexOf("[\"") + 2, it.indexOf("\"]"))
                println("Testing $directory")
            } else if (it.startsWith("    ") && it.endsWith(";")) {
                val str = it.substring(4, 4 + max(it.indexOf(".eta"), it.indexOf(".eti")))
                Etac(disableOutput = true).main(
                    listOf(
                        "-libpath", "src/tests/${directory}", "--irgen", "--parse", "--lex", "src/tests/${directory}/${str}"
                    )
                )
            }
        }
    }
}