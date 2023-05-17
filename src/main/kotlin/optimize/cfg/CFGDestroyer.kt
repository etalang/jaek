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
    private val sett = mutableSetOf<String>()

    init {
        mm.nodesWithJumpInto().forEach { jumpLabels[it] = freshLabel() }
        val parents = listOf(cfg.start) + mm.nodesWithNoFallThroughsMinusStart()
        parents.forEach {
            var node: CFGNode? = it
            while (node != null) {
                when (node) {
                    is Funcking -> {
                        val retMoves = node.movIntos.map {
                            when (it) {
                                is Gets -> LIRMove(LIRTemp(it.varName), translateExpr(it.expr))
                                is CFGNode.Mem -> LIRMove(translateExpr(it.loc), translateExpr(it.expr))
                            }
                        }
                        addStmt(
                            node,
                            LIRCallStmt(
                                LIRName(node.name),
                                node.movIntos.size.toLong(),
                                node.args.map { translateExpr(it) })
                        )
                        stmts.addAll(retMoves)
                    }

                    is If -> node.let { n ->
                        mm.jumpingTo(n)
                            ?.let { addStmt(n, LIRTrueJump(translateExpr(n.cond), jumpLabels[it]!!)) }
                    }

                    is Gets -> addStmt(node, LIRMove(LIRTemp(node.varName), translateExpr(node.expr)))

                    is CFGNode.Mem -> addStmt(node, LIRMove(translateExpr(node.loc), translateExpr(node.expr)))

                    is Return -> addStmt(node, LIRReturn(node.rets.map { translateExpr(it) }))

                    is Start -> {}

                    is Cricket -> node.let { n ->
                        mm.jumpingTo(n)
                            ?.let { addStmt(n, LIRJump(LIRName(jumpLabels[it]!!.l))) }
                    }

                    is NOOP -> node.let { n ->
                        mm.jumpingTo(n)
                            ?.let { addStmt(n, LIRJump(LIRName(jumpLabels[it]!!.l))) }
                    }
                }
                node = mm.fallThrough(node)
            }
        }
        body = LIRSeq(stmts)
    }

    private fun addStmt(node: CFGNode, stmt: FlatStmt) {
        jumpLabels[node]?.let { stmts.add(it); sett.add(it.l) }
        stmts.add(stmt)
    }

    fun destroy(): LIRFuncDecl {
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