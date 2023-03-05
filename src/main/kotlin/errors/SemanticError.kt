package errors

class SemanticError(line: Int, col: Int, val desc: String, file: String) : CompilerError(
    line, col, "Semantic Error",
    file
) {
    override val mini: String = "${line}:${column} error: ${desc}"

    override val log: String =
        "Semantic error beginning at ${file}:${line}:${column}: ${desc}"
}