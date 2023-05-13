package assembly

import assembly.ralloc.ChaitinRegisterAllocator
import assembly.ralloc.RegisterAllocator
import assembly.ralloc.TrivialRegisterAllocator
import ir.lowered.LIRCompUnit
import typechecker.EtaFunc

class AssemblyGenerator(val ir: LIRCompUnit, val functionType: Map<String, EtaFunc>, val chaitin : Boolean = true) {
    fun generate(): String {
        val compUnit = ir.abstractAssembly()
        val ra : RegisterAllocator = if (chaitin)
            ChaitinRegisterAllocator(compUnit, functionType)
            else TrivialRegisterAllocator(compUnit, functionType)
        return ra.allocate().toString()
    }
}