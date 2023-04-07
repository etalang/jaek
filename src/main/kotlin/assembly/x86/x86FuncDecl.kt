package assembly.x86

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
        // calculate amount to allocate
        funcBlock.add("enter 8000, 0")
        return funcBlock.joinToString("\n")
    }
}