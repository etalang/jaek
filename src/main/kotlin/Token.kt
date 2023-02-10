import java_cup.runtime.Symbol

//TODO: still need to figure out Symbol init
open class Token(val lexeme: String , val line: Int, val col : Int) : Symbol(0) {

    open fun positionInfo(): String? {
        return "$line:$col"
    }

    override fun toString(): String {
        return positionInfo() + " " + lexeme
    }

}

internal class StringToken(charBuffer: ArrayList<Int>, lineNum: Int, strStart: Int) :
    Token(LexUtil.getStringRepresentation(charBuffer), lineNum, strStart) {
    var attribute: String

    init {
        attribute = LexUtil.getStringRepresentation(charBuffer)
    }

    override fun toString(): String {
        return positionInfo() + " string " + attribute
    }
}

internal class IntegerToken(lex: String, lineNum: Int, col: Int) : Token(lex, lineNum, col) {
    var attribute: Long

    init {
        attribute = LexUtil.parseToInt(lex)
    }

    override fun toString(): String {
        return positionInfo() + " integer " + attribute
    }
}
internal class CharacterToken(lex: String, lineNum: Int, col: Int) : Token(lex, lineNum, col) {
    var attribute // the integer represents the character
            : Int

    init {
        attribute = LexUtil.parseToChar(lex.substring(1, lex.length - 1), lineNum, col)
    }

    override fun toString(): String {
        return positionInfo() + " character " + LexUtil.formatChar(attribute)
    }
}

internal class KeywordToken(lex: String, lineNum: Int, col: Int) : Token(lex, lineNum, col)

internal class IdToken(lex: String, lineNum: Int, col: Int) : Token(lex, lineNum, col) {
    override fun toString(): String {
        return positionInfo() + " id " + lexeme
    }
}

internal class SymbolToken(lex: String, lineNum: Int, col: Int) : Token(lex, lineNum, col)