package ir

import edu.cornell.cs.cs4120.etac.ir.IRNode as JIRNode

/** InterRep represents the intermediate representation**/
abstract class InterRep {
    abstract val java: JIRNode
}