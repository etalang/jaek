package ir

import edu.cornell.cs.cs4120.etac.ir.IRNode
import edu.cornell.cs.cs4120.etac.ir.IRNodeFactory_c

/** InterRep represents the intermediate representation**/
abstract class InterRep {
    val factory = IRNodeFactory_c()
    abstract val java: IRNode
}