package assembly.x86

class x86FuncDecl(val name : String, val body : List<Instruction>) {
    override fun toString(): String {
        val funcBlock = mutableListOf<String>(
            ".globl $name",
            ".type $name, @function",
            ".align 32",
            "$name:",
//            "and rsp, -16" // force rsp to be aligned on entry
        )
        for (insn in body) {
            funcBlock.add(insn.toString())
        }
        funcBlock.add(".size $name, .-$name")
        return funcBlock.joinToString("\n")
    }
}