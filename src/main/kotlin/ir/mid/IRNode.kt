package ir.mid

import edu.cornell.cs.cs4120.etac.ir.IRNode
import edu.cornell.cs.cs4120.etac.ir.IRNodeFactory_c
import ir.InterRep
import ir.lowered.LIRNode

/** IRNode represents a node in the intermediate representation abstract syntax tree**/
sealed class IRNode : InterRep() {
    abstract val java: IRNode //TODO: REMOVE THIS ONCE WE LOWER!
//    abstract val lower : LIRNode
    //TODO: UNCOMMENT ABOVE ^
}