package assembly

import assembly.x86.Register
import ir.lowered.LIRCompUnit
import typechecker.EtaType

class AssemblyGenerator(val ir: LIRCompUnit, val functionType: Map<String, EtaType.ContextType.FunType>) {
    fun generate(): String {
        val compUnit = ir.abstractAssembly()
        val ra = RegisterAllocator(compUnit, functionType)
        return ra.allocate().toString()
    }
}