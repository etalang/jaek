package ir.mid
import ast.*
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType
import edu.cornell.cs.cs4120.etac.ir.IRBinOp as JIRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRCall as JIRCall
import edu.cornell.cs.cs4120.etac.ir.IRConst as JIRConst
import edu.cornell.cs.cs4120.etac.ir.IRESeq as JIRESeq
import edu.cornell.cs.cs4120.etac.ir.IRMem as JIRMem
import edu.cornell.cs.cs4120.etac.ir.IRName as JIRName
import edu.cornell.cs.cs4120.etac.ir.IRTemp as JIRTemp

/** IRExpr represents an expression**/
sealed class IRExpr : IRNode() {
    /** IRConst(value) represents an integer constant [value]**/
    class IRConst(val value: Long) : IRExpr() {
        override val java: JIRConst = TODO("Not yet implemented")

    }

    /** IRTemp(name) represents a temporary register or value named [name] **/
    class IRTemp(val name: String) : IRExpr() {
        override val java: JIRTemp = TODO("Not yet implemented")

    }

    /** IROp(left,right) represents the evaluation of an arithmetic, logical, or relational
     * operation on the evaluated expressions of [left] and [right]**/
    class IROp(val op : OpType, left: IRExpr, val right: IRExpr) : IRExpr() {
        override val java: JIRBinOp = TODO("Not yet implemented")

    }

    class IRMem(val address: IRExpr) : IRExpr() {
        override val java: JIRMem = TODO("Not yet implemented")

    }

    /** IRCall(address,args) represents a function call on a function code with address [address]
     * and arguments [args]**/
    class IRCall(val address: IRExpr, val args: ArrayList<IRExpr>) : IRExpr() {
        override val java: JIRCall = TODO("Not yet implemented")
    }

    /** IRName(l) represents the address of a labeled memory address labeled [l]*/
    class IRName(val l: String) : IRExpr() {
        override val java: JIRName = TODO("Not yet implemented")
    }

    /** IRESeq(statement, value) represents the result of [value] after executing [statement] **/
    class IRESeq(val statement: IRStmt, val value: IRExpr) : IRExpr() {
        override val java: JIRESeq = TODO("Not yet implemented")
    }
}
