package assembly

import ir.lowered.LIRCompUnit
import typechecker.EtaFunc

class AssemblyGenerator(val ir: LIRCompUnit, val functionType: Map<String, EtaFunc>) {
    fun generate(): String {
        val compUnit = ir.abstractAssembly()
        val ra = RegisterAllocator(compUnit, functionType)
        return ra.allocate().toString()
    }
}