package optimize.cfg

import ir.lowered.*
import ir.lowered.LIRExpr.*
import ir.lowered.LIRStmt.*
import optimize.cfg.CFGExpr.*
import optimize.cfg.CFGExpr.Mem
import optimize.cfg.CFGNode.*

class CFGDestroyer(val cfg: CFG, val func: LIRFuncDecl) {
    val body: LIRSeq
    val visited: MutableSet<LabelNode> = mutableSetOf()
    val stack: ArrayDeque<LabelNode> = ArrayDeque()
    val stmts = mutableListOf<FlatStmt>()
    val toJumpLabels: MutableMap<CFGNode, LIRLabel> = mutableMapOf()

    init {
        val predEdges = cfg.getPredEdges()
        stack.addFirst(LabelNode(cfg.start))
        while (stack.isNotEmpty()) {
            val labeledNode = stack.removeFirst()
            visited.add(labeledNode)
            labeledNode.label?.let { stmts.add(it) } // if we said this node would need a jump label, add the label first
            predEdges[labeledNode.node]?.let {
                if (it.size > 1) {
                    if (!toJumpLabels.contains(labeledNode.node)) {
                        val jumpLabel = freshLabel()
                        stmts.add(jumpLabel)
                        toJumpLabels[labeledNode.node] = jumpLabel
                    } else {
                        stmts.add(toJumpLabels[labeledNode.node]!!)
                    }
                }
            }

            when (val node = labeledNode.node) {
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
                    addFallThrough(node) // add the following node which we'd like to do RIGHT NEXT SO MUST BE ON TOP
                }

                is If -> {
                    val trueLabel = freshLabel()
                    stmts.add(LIRTrueJump(translateExpr(node.cond), trueLabel))
                    addFallThrough(node)
                    addTake(node, visited, stack, trueLabel)
                    // need to jump back to "end" of if statement after ? check where the crickets WERE and add in jumps
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
                is Cricket -> throw Exception("cease your cricketing")
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
            if (!visited.contains(LabelNode(it.node))) {
                stack.addFirst(LabelNode(it.node)) // since it's a fall-through, we do it right now
            }
        }
    }

    fun addTake(node: If, visited: Set<LabelNode>, stack: ArrayDeque<LabelNode>, newLabel: LIRLabel) {
        node.take?.let { if (!visited.contains(LabelNode(it.node))) stack.addLast(LabelNode(it.node, newLabel)) }
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

    private var freshLabelCount = 0

    private fun freshLabel(): LIRLabel {
        freshLabelCount++
        return LIRLabel("\$NI$freshLabelCount")
    }

    data class LabelNode(val node: CFGNode, val label: LIRLabel? = null) {
        override fun equals(other: Any?): Boolean {
            return if (!(other is LabelNode)) false else other.node == node
        }
    }
}