package errors

class SemanticError(line: Int, col: Int, val desc: String) : CompilerError(line, col, "Semantic Error") {
    override val mini: String = "${line}:${column} error: ${desc}"

    override fun log(file: String): String {
        return "Semantic error beginning at ${file}:${line}:${column}: ${desc}"
    }
}