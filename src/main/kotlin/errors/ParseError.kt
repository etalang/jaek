package errors

import Token
import java_cup.runtime.Symbol
import java.io.File

class ParseError(val sym: Symbol, file: File) : CompilerError(
    when (sym) {
        is Token<*> -> sym.line
        else -> {
            sym.left
        } //LOL I SHOULD NOT HAVE BOTH HERE BUT THIS IS BETTER THAN NOTHING I GUESS
    }, when (sym) {
        is Token<*> -> sym.col
        else -> {
            sym.right
        }
    }, "Syntax Error", file
) {
    override val log: String = "Syntax error beginning at ${file.name}:${line}:${column}${
        when (sym) {
            is Token<*> -> ": ${sym.stringVal()}"
            else -> ""
        }
    }"


    override val mini: String = "${line}:${column} error${
        when (sym) {
            is Token<*> -> ": ${sym.stringVal()}"
            else -> ""
        }
    }"


}