package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType
import edu.cornell.cs.cs4120.etac.ir.IRBinOp as JIRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRCall as JIRCall
import edu.cornell.cs.cs4120.etac.ir.IRConst as JIRConst
import edu.cornell.cs.cs4120.etac.ir.IRExpr as JIRExpr
import edu.cornell.cs.cs4120.etac.ir.IRMem as JIRMem
import edu.cornell.cs.cs4120.etac.ir.IRName as JIRName
import edu.cornell.cs.cs4120.etac.ir.IRTemp as JIRTemp

/** IRExpr represents an expression**/
sealed class LIRExpr : LIRNode() {
    override abstract val java: JIRExpr;

    /** IRConst(value) represents an integer constant [value]**/
    class LIRConst(val value: Long) : LIRExpr() {
        override val java: JIRConst = factory.IRConst(value)

    }

    /** IRTemp(name) represents a temporary register or value named [name] **/
    class LIRTemp(val name: String) : LIRExpr() {
        override val java: JIRTemp = factory.IRTemp(name)

    }

    /** IROp(left,right) represents the evaluation of an arithmetic, logical, or relational
     * operation on the evaluated expressions of [left] and [right]**/
    class LIROp(val op: OpType, val left: LIRExpr, val right: LIRExpr) : LIRExpr() {
        override val java: JIRBinOp = factory.IRBinOp(op, left.java, right.java)

    }

    /** IRMem(address) evaluates [address] and looks up the memory contents in [address]**/
    class LIRMem(val address: LIRExpr) : LIRExpr() {
        override val java: JIRMem = factory.IRMem(address.java)

    }

    /** IRName(l) represents the address of a labeled memory address labeled [l]*/
    class LIRName(val l: String) : LIRExpr() {
        override val java: JIRName = factory.IRName(l)
    }
}
