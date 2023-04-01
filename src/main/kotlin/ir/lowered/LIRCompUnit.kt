package ir.lowered

import ir.IRData
import edu.cornell.cs.cs4120.etac.ir.IRCompUnit as JIRCompUnit

/** IRCompUnit represents a compilation unit**/
class LIRCompUnit(
    val name: String, val functions: List<LIRFuncDecl>, val globals: List<IRData>
) : LIRNode() {
    private var freshLabelCount =0

    fun reorderBlocks() {
        functions.forEach { it.reorderBlocks(this::freshLabel) }
    }

    private fun freshLabel(): String {
        freshLabelCount++
        return "\$B$freshLabelCount"
    }

    fun constantFold() {
        TODO("Not yet implemented")
    }

    override val java: JIRCompUnit
        get() {
            val _java = factory.IRCompUnit(
                name, HashMap(functions.associate { it.name to it.java })
            ) //TODO: do we copy?
            globals.forEach {
                _java.appendData(it.java)
            }
            return _java
        }
}