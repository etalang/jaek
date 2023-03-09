package ir.mid

import edu.cornell.cs.cs4120.etac.ir.IRNodeFactory_c
import ir.InterRep

/** IRNode represents a node in the intermediate representation abstract syntax tree**/
sealed class IRNode : InterRep() {
    val factory = IRNodeFactory_c()
}