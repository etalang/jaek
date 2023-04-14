package assembly

import ir.lowered.LIRCompUnit
import typechecker.EtaType

class AssemblyGenerator(val ir: LIRCompUnit, val functionType: Map<String, EtaType.ContextType.FunType>) {
    fun generate(): String {
        val compUnit = ir.abstractAssembly()
        return compUnit.toString()
    }
}