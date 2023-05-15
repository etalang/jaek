package optimize.cfg

import ir.lowered.*
import ir.lowered.LIRExpr.*
import ir.lowered.LIRStmt.*
import optimize.cfg.CFGExpr.*
import optimize.cfg.CFGExpr.Mem
import optimize.cfg.CFGNode.*

class CFGDestroyer(val cfg: CFG, val func: LIRFuncDecl) {
    val body: LIRSeq
    private val visited: MutableSet<CFGNode> = mutableSetOf()
    val stack: ArrayDeque<CFGNode> = ArrayDeque()
    val stmts = mutableListOf<FlatStmt>()
    private val jumpLabels: MutableMap<CFGNode, LIRLabel> = mutableMapOf()
    private val mm = cfg.mm

    init {
        mm.fastNodesWithPredecessors().forEach { jumpLabels[it] = freshLabel() }
        stack.addFirst(cfg.start)
        while (stack.isNotEmpty()) {
            val node = stack.removeFirst()
            visited.add(node)

            when (node) {
                is Funcking -> {
                    val retMoves = node.movIntos.map {
                        when (it) {
                            is Gets -> LIRMove(LIRTemp(it.varName), translateExpr(it.expr))
                            is CFGNode.Mem -> LIRMove(translateExpr(it.loc), translateExpr(it.expr))
                        }
                    }
                    stmts.add(
                        LIRCallStmt(
                            LIRName(node.name),
                            node.movIntos.size.toLong(),
                            node.args.map { translateExpr(it) })
                    )
                    stmts.addAll(retMoves)
                }

                is If -> mm.jumpingTo(node)?.let { stmts.add(LIRTrueJump(translateExpr(node.cond), jumpLabels[it]!!)) }

                is Gets -> stmts.add(LIRMove(LIRTemp(node.varName), translateExpr(node.expr)))

                is CFGNode.Mem -> stmts.add(LIRMove(translateExpr(node.loc), translateExpr(node.expr)))

                is Return -> stmts.add(LIRReturn(node.rets.map { translateExpr(it) }))

                is Start -> {}

                is Cricket -> {}
            }
            handleNext(node)
        }
        body = LIRSeq(stmts)
    }

    private fun handleNext(node: CFGNode) {
        mm.fallThrough(node)?.let {
            stack.addFirst(it)
        }
        mm.jumpingTo(node)?.let {
            stmts.add(LIRJump(LIRName(jumpLabels[it]!!.l)))
        }
    }

    fun destroy(): LIRFuncDecl {
        println("DEATH")
        return LIRFuncDecl(cfg.function, body)
    }

    private fun translateExpr(it: CFGExpr): LIRExpr {
        return when (it) {
            is BOp -> LIROp(it.op, translateExpr(it.left), translateExpr(it.right))
            is Const -> LIRConst(it.value)
            is Label -> LIRName(it.name)
            is Mem -> LIRMem(translateExpr(it.loc))
            is Var -> LIRTemp(it.name)
        }
    }

    companion object {
        private var freshLabelCount = 0
        private fun freshLabel(): LIRLabel {
            freshLabelCount++
            return LIRLabel("\$X${freshLabelCount}")
        }
    }
}