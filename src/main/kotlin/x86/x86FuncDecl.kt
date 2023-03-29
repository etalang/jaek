package x86

class x86FuncDecl(val name : String, val body : List<Instruction>) {
    override fun toString(): String {
        var funcBlock = mutableListOf<String>(
            ".globl $name",
            ".type $name, @function",
            "$name:"
        )
        for (insn in body) {
            funcBlock.add(insn.toString())
        }
        funcBlock.add(".size $name, .-$name")
        return funcBlock.joinToString("\n")
    }
}