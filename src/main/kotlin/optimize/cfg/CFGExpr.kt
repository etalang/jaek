package optimize.cfg

import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*

sealed class CFGExpr {
    companion object {
        fun opString(op : IRBinOp.OpType) : String {
            return when (op) {
                ADD -> "+"
                SUB -> "-"
                MUL -> "*"
                HMUL -> "*>>"
                DIV -> "/"
                MOD -> "%"
                AND -> "&"
                OR -> "|"
                XOR -> "⊕"
                LSHIFT -> "<<"
                RSHIFT -> ">>"
                ARSHIFT -> "A<<"
                EQ -> "="
                NEQ -> "!="
                LT -> "<"
                ULT -> "u<"
                GT -> ">"
                LEQ -> "<="
                GEQ -> ">="
            }
        }
    }
    abstract val pretty: String

    class Var(val name : String) : CFGExpr() {
        override val pretty = name
    }
    class Const(val value : Long) : CFGExpr() {
        override val pretty = value.toString()
    }
    class BOp(val left: CFGExpr, val right: CFGExpr, val op : IRBinOp.OpType) : CFGExpr() {
        override val pretty = "${left.pretty} ${opString(op)} ${right.pretty}"
    }
    class Mem(val loc: CFGExpr) : CFGExpr() {
        override val pretty = loc.pretty
    }
//    class Func(val args: List<CFGExpr>) : CFGExpr() // TODO: determine what "function calls at top level" means
}