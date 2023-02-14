package ast

sealed class Type {
    enum class Primitive { INT, BOOL }
    data class Array(val t: Type) //sorta see where you're going with this?
}