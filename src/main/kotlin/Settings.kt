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
//        constructor(phases: List<String>, sourceDir: String) : this(
//            if (phases.contains("initial")) File("") else null, if (phases.contains("final")) File("") else null
//        )
        //TODO: KATE
    }
}