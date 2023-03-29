package x86

import ir.IRData

class x86CompUnit(val name : String, val functions: List<x86FuncDecl>, val globals: List<x86Data>) {
    override fun toString(): String {
        var fullText = mutableListOf<String>(
            ".file \"$name.eta\"",
            ".intel_syntax noprefix",
        )
        if (globals.isNotEmpty()) {
            fullText.add("")
            fullText.add(".data")
            for (data in globals) {
                fullText.add(data.toString())
            }
        }
        fullText.add("")
        fullText.add(".data")
        for (func in functions) { // cannot be empty
            fullText.add(func.toString())
        }
        return fullText.joinToString("\n")
    }
}