package ir

import assembly.x86.x86Data
import edu.cornell.cs.cs4120.etac.ir.IRData as JIRData

/** IRData represents static data (such as global variables) **/
class IRData(val name: String, val data: LongArray) {
    val java: JIRData = JIRData(name, data)

    val tile get() = x86Data(name, data)
}