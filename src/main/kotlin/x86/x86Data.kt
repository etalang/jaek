package x86

class x86Data(val name : String, val data : LongArray) {
    override fun toString(): String {
        val sizeInBytes = 8 * data.size
        var dataBlock = mutableListOf<String>(
            ".global $name",
            ".align 32", // this is potentially wrong
            ".type $name, @object",
            ".size $name, $sizeInBytes",
            "$name:"
        )
        for (elt in data) {
            dataBlock.add(".quad $elt")
        }
        return dataBlock.joinToString("\n")
    }
}