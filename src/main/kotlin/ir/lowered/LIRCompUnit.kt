package ir.lowered

import assembly.x86.x86CompUnit
import assembly.x86.x86Data
import assembly.x86.x86FuncDecl
import ir.IRData
import edu.cornell.cs.cs4120.etac.ir.IRCompUnit as JIRCompUnit

/** IRCompUnit represents a compilation unit**/
class LIRCompUnit(
    val name: String, val functions: List<LIRFuncDecl>, val globals: List<IRData>
) : LIRNode() {

    fun reorderBlocks() {
        functions.forEach { it.reorderBlocks() }
    }

    fun abstractAssembly(): x86CompUnit {
        val assemblyFuncs: MutableList<x86FuncDecl> = ArrayList()
        functions.forEach { assemblyFuncs.add(it.tile) }
        val assemblyData: MutableList<x86Data> = ArrayList()
        globals.forEach { assemblyData.add(it.tile) }
        return x86CompUnit(name, assemblyFuncs, assemblyData)
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