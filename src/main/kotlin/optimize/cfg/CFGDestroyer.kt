package optimize.cfg

import ir.lowered.*
import ir.lowered.LIRExpr.*
import ir.lowered.LIRStmt.*
import optimize.cfg.CFGExpr.*
import optimize.cfg.CFGExpr.Mem
import optimize.cfg.CFGNode.*

class CFGDestroyer(val cfg: CFG, val func: LIRFuncDecl) {
    val body: LIRSeq
    val visited: MutableSet<CFGNode> = mutableSetOf()
    val stack: ArrayDeque<CFGNode> = ArrayDeque()
    val stmts = mutableListOf<FlatStmt>()
    private val toJumpLabels: MutableMap<CFGNode, LIRLabel> = mutableMapOf()

    init {
        val predEdges = cfg.getPredEdges()
        stack.addFirst(cfg.start)
        while (stack.isNotEmpty()) {
            val node = stack.removeFirst()
            visited.add(node)
            predEdges[node]?.let {
                val jumpPreds = it.filter { it.jump }
                if (jumpPreds.isNotEmpty()) {
                    if (!toJumpLabels.contains(node)) {
                        val jumpLabel = freshLabel()
                        stmts.add(jumpLabel)
                        toJumpLabels[node] = jumpLabel
                    } else {
                        stmts.add(toJumpLabels[node]!!)
                    }
                }
            }

            when (node) {
                is Funcking -> {
                    val retMoves = node.movIntos.map {
                        when (it) {
                            is Gets -> LIRMove(LIRTemp(it.varName), translateExpr(it.expr))
                            is CFGNode.Mem -> LIRMove(translateExpr(it.loc), translateExpr(it.expr))
                        }
                    }
                    stmts.add(
                        LIRCallStmt(LIRName(node.name),
                            node.movIntos.size.toLong(),
                            node.args.map { translateExpr(it) })
                    )
                    stmts.addAll(retMoves)
                    addFallThrough(node) // add the following node which we'd like to do RIGHT NEXT SO MUST BE ON TOP
                }

                is If -> {
                    if (toJumpLabels.contains(node)) { // if we have seen the node we're jumping to
                        stmts.add(
                            LIRTrueJump(
                                translateExpr(node.cond), toJumpLabels[node]!!
                            )
                        ) // jump to pre-inserted label
                    } else { // we have not seen the node we're jumping to
                        val trueLabel = freshLabel()
                        node.take?.node?.let { toJumpLabels[it] = trueLabel } // label before whatever the true jump is
                        stmts.add(LIRTrueJump(translateExpr(node.cond), trueLabel))
                    }
                    addFallThrough(node)
                    addTake(node)
                }

                is Gets -> {
                    stmts.add(LIRMove(LIRTemp(node.varName), translateExpr(node.expr)))
                    addFallThrough(node) // might be able to factor this out depending on what goes down with IF
                }

                is CFGNode.Mem -> {
                    stmts.add(LIRMove(translateExpr(node.loc), translateExpr(node.expr)))
                    addFallThrough(node)// might be able to factor this out depending on what goes down with IF
                }

                is Return -> {
                    stmts.add(LIRReturn(node.rets.map { translateExpr(it) }))
                }

                is Start -> addFallThrough(node) // does not exist in lir
                is Cricket -> addFallThrough(node)
            }
        }
//        if (func.numRets < 1) stmts.add(LIRReturn(listOf()))
        body = LIRSeq(stmts)
    }

    private fun addFallThrough(node: CFGNode) {
        node.to?.let {
            if (it.jump) { // if the fallthrough is an uncond jump
                if (toJumpLabels.contains(it.node)) { // if we have seen the node we're jumping to
                    stmts.add(LIRJump(LIRName(toJumpLabels[it.node]!!.l))) // jump to pre-inserted label
                } else { // we have not seen the node we're jumping to
                    val jumpLabel = freshLabel()
                    toJumpLabels[it.node] = jumpLabel
                    stmts.add(LIRJump(LIRName(jumpLabel.l))) // jump to imaginary label that will be inserted later
                }
            }
            if (!visited.contains(it.node)) {
                stack.addFirst(it.node) // since it's a fall-through, we do it right now
            }
        }
    }

    fun addTake(node: If) {
        node.take?.let { if (!visited.contains(it.node)) stack.addLast(it.node) }
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