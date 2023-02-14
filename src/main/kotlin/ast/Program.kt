package ast

data class Program(
    val imports : ArrayList<Use>,
    val definitions : ArrayList<Defn>
) : Node

