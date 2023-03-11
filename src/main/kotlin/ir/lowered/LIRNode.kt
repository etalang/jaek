package ir.lowered

import ir.InterRep
import edu.cornell.cs.cs4120.etac.ir.IRNode as JIRNode

/** IRNode represents a node in the intermediate representation abstract syntax tree**/
sealed class LIRNode : InterRep() {
    abstract val java: JIRNode
}