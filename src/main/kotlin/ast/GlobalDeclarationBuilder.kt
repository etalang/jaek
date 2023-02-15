package ast

class GlobalDeclarationBuilder(
    val decls : MutableList<GlobalDecl>,
    val vals : MutableList<Literal>?) : Definition()  {
    fun toDefinitionList() :ArrayList<Definition> {
        return ArrayList();}
}