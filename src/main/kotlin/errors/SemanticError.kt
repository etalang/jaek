package errors

class SemanticError(line: Int, col: Int, val desc: String) : CompilerError(line, col, "Semantic Error") {
    override fun output(file: String): String {
        return "Semantic error beginning at ${file}:${line}:${column}: ${desc}"
    }
}