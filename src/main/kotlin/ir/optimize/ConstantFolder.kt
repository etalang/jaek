package ir.optimize

import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import ir.lowered.LIRExpr
import ir.lowered.LIROp
import java.math.BigInteger

class ConstantFolder : IROptimizer() {
    companion object {
        private fun lowMul(n1: Long, n2: Long): Long {
            val sgn1 = if (n1 >= 0) 1 else -1
            val sgn2 = if (n2 >= 0) 1 else -1
            val n1H = (n1 * sgn1) ushr 32
            val n1L = ((n1 * sgn1) shl 32) ushr 32
            val n2H = (n2 * sgn2) ushr 32
            val n2L = ((n2 * sgn2) shl 32) ushr 32
            return ((n1L * n2L) + ((n1H * n2L) shl 32) + ((n2H * n1L) shl 32)) * sgn1 * sgn2
        }

        private fun highMul(n1: Long, n2: Long): Long {
            return BigInteger.valueOf(n1).multiply(BigInteger.valueOf(n2))
                .shiftRight(64).longValueExact()
        }

        fun calculate(n1: Long, n2: Long, op: IRBinOp.OpType): Long {
            return when (op) {
                IRBinOp.OpType.ADD -> n1 + n2
                IRBinOp.OpType.SUB -> n1 - n2
                IRBinOp.OpType.MUL -> lowMul(n1, n2)
                IRBinOp.OpType.HMUL -> highMul(n1, n2)
                IRBinOp.OpType.DIV -> n1 / n2
                IRBinOp.OpType.MOD -> n1 % n2
                IRBinOp.OpType.AND -> n1 and n2
                IRBinOp.OpType.OR -> n1 or n2
                IRBinOp.OpType.XOR -> n1 xor n2
                IRBinOp.OpType.LSHIFT -> n1 shl n2.toInt()
                IRBinOp.OpType.RSHIFT -> n1 ushr n2.toInt()
                IRBinOp.OpType.ARSHIFT -> n1 shr n2.toInt()
                IRBinOp.OpType.EQ -> if (n1 == n2) 1 else 0
                IRBinOp.OpType.NEQ -> if (n1 != n2) 1 else 0
                IRBinOp.OpType.LT -> if (n1 < n2) 1 else 0
                IRBinOp.OpType.ULT -> if (n1.toULong() < n2.toULong()) 1 else 0
                IRBinOp.OpType.GT -> if (n1 > n2) 1 else 0
                IRBinOp.OpType.LEQ -> if (n1 <= n2) 1 else 0
                IRBinOp.OpType.GEQ -> if (n1 >= n2) 1 else 0
            }
        }
    }
    override fun applyOp(node: LIROp): LIRExpr {
        val left = applyExpr(node.left)
        val right = applyExpr(node.right)

        if (left is LIRExpr.LIRConst && right is LIRExpr.LIRConst
            && !(node.op == IRBinOp.OpType.DIV && right.value == 0L)
        ) {
            return LIRExpr.LIRConst(calculate(left.value, right.value, node.op))
        }
        return super.applyOp(node)
    }

}