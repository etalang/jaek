package ast

sealed class Type : Node {
    enum class Primitive { INT, BOOL }
    data class Array (val t : Type)
}