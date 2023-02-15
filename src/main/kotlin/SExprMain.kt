import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter
import java.io.FileOutputStream
import java.io.PrintWriter

class SExprMain {
    fun main() {
//        print("hello!")
        val expr = CodeWriterSExpPrinter(PrintWriter("test.parsed"))
        expr.printAtom("test!!")
        expr.startList()
        expr.printAtom("INSIDE!")
        expr.endList()
        expr.close()
    }
}

fun main(args: Array<String>) = SExprMain().main()