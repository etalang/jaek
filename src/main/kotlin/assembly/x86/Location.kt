package assembly.x86

class Location (val l : Label) {
    override fun toString(): String {
        return l.toString()
    }
//    data class LabelLoc(val l : Label) : Location() {
//        override fun toString(): String {
//            return l.toString()
//        }
//    }
//
//    data class ConstLoc(val l : Long) : Location() {
//        override fun toString(): String {
//            return l.toString()
//        }
//    }
}