package errors

import java.io.File

class SemanticError(line: Int, col: Int, val desc: String, file: File) : CompilerError(
    line, col, "Semantic Error",
    file
) {
    override val mini: String = "${line}:${column} error: ${desc}"

    override val log: String =
        "Semantic error beginning at ${file.name}:${line}:${column}: ${desc}"
}