import java.io.File

sealed class Settings {
    sealed class Opt {
        enum class Actions { cf, reg, copy, dce }

        abstract fun desire(optimization: Actions): Boolean

        object All : Opt() {
            override fun desire(optimization: Actions): Boolean = true
        }

        object None : Opt() {
            override fun desire(optimization: Actions): Boolean = false
        }

        class Of(private val options: List<Actions>) : Opt() {
            override fun desire(optimization: Actions): Boolean = options.contains(optimization)
        }
    }

    class OutputIR(val initial: File?, val final: File?) {
    }

    class OutputCFG(val initial: File?, val final: File?) {
        //file expected is just path to desired output location + file name and any extension, no phase, no func
        fun getOutInit(name : String) : File? {
            return if (initial != null){
                val funcFile =
                    File(initial.parent, initial.nameWithoutExtension + "_${name}_initial.dot")
                if (funcFile.exists() && !funcFile.isDirectory) {
                    funcFile.delete()
                }
                funcFile.createNewFile()
                funcFile
            } else null
        }
    }
}