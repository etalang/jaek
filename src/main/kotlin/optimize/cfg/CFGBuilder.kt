package optimize.cfg

import ir.lowered.*
import java.util.*

class CFGBuilder(val lir: LIRFuncDecl) {
    val targets: MutableMap<String, CFGNode> = mutableMapOf()
    private val nodes: MutableList<CFGNode>
    private var pointingTo: String? = null;
    val start: CFGNode.Start

    init {
        start = CFGNode.Start(lir.name, pointToNext())
        nodes = mutableListOf(start)

        val statements: MutableList<LIRStmt.FlatStmt> = LinkedList(lir.body.block)
        while (statements.isNotEmpty()) {
            var currStmt = statements.first()

            //handle labels
            val labels = (listOfNotNull(pointingTo).toMutableList())
            while (currStmt is LIRStmt.LIRLabel) {
                labels.add(currStmt.l)
                statements.removeFirst()
                currStmt = statements.first()
            }

            currStmt = statements.removeFirst()

            //create nodes
            val next = when (currStmt) {
                is LIRStmt.LIRCJump -> throw Exception("honestly shout out to charles for somehow sneaking a CJUMP in this far after block reordering")

                is LIRStmt.LIRJump -> {
                    CFGNode.Cricket(Edge.Lazy(targets, currStmt.address.l))
                }

                is LIRReturn -> {
                    pointingTo = null
                    CFGNode.Return(currStmt.valList.map { translateExpr(it) }, Edge.Lazy(targets, null))
                }

                is LIRStmt.LIRTrueJump -> CFGNode.If(
                    translateExpr(currStmt.guard), Edge.Lazy(targets, currStmt.trueBranch.l), pointToNext()
                )

                is LIRCallStmt -> {
                    val returnMoves = mutableListOf<String>()
                    for (i in 0 until currStmt.n_returns.toInt()) {
                        when (val movLIR = statements.removeFirst()) {
                            is LIRMove -> {
                                val dest = movLIR.dest
                                if (dest is LIRExpr.LIRTemp) returnMoves.add(dest.name)
                                else throw Exception("mooooooving into non-temp")
                            }

                            else -> throw Exception("charles was really hoping this would be a move, and we were too :(")
                        }
                    }
                    CFGNode.Funcking(
                        currStmt.target.l, returnMoves, currStmt.args.map { translateExpr(it) }, pointToNext()
                    )
                }

                is LIRStmt.LIRLabel -> throw Exception("charles snuck more labels in")
                is LIRMove -> when (val dest = currStmt.dest) {
                    is LIRExpr.LIRTemp -> {
                        CFGNode.Gets(dest.name, translateExpr(currStmt.expr), pointToNext())
                    }

                    is LIRMem -> {
                        CFGNode.Mem(translateExpr(dest), translateExpr(dest), pointToNext())
                    }

                    else -> throw Exception("move has a non mem non temp and @kate said that's illegal")
                }
            }

            nodes.add(next)
            labels.forEach {
                val target = targets[it]
                if (target == null) targets[it] = next;
            }
        }

        nodes.forEach { it.resolveEdges() }
    }

    fun graphViz(): String {
        val map = mutableMapOf<CFGNode, String>()
        nodes.filter { it !is CFGNode.Cricket }.forEachIndexed { index, t -> map[t] = "n$index" }
        return buildString {
            appendLine("digraph ${lir.name} {")
            appendLine("\trankdir=\"TB\"")
            appendLine("\tfontname = \"Helvetica,Arial,sans-serif\";")
            appendLine("\tnode [fontname = \"Helvetica,Arial,sans-serif\";];")
            appendLine("\tedge [fontname = \"Helvetica,Arial,sans-serif\";];")

            map.forEach { appendLine("\t${it.value} [shape=rectangle; label=\"${it.key.pretty}\";];") }
            map.forEach { (from, graphKey) ->
                for (edge in from.edges) {
                    appendLine("\t$graphKey -> ${map[edge.node]} ${if (edge.jump) "[label=\"jump\"]" else ""};")
                }
//                when (from) {
//                    is CFGNode.If -> {
//                        if (from.to != null) appendLine("\t$graphKey -> ${map[from.to?.node]} [label=\"T\"];")
//                        if (from.take?.node != null) appendLine("\t$graphKey -> ${map[from.take?.node]} [label=\"F\"];")
//                    }
//
//                    is CFGNode.Cricket, is CFGNode.Funcking, is CFGNode.Gets, is CFGNode.Mem, is CFGNode.Return, is CFGNode.Start -> {
//
//
//
//                        if (from.to?.node != null) appendLine("\t$graphKey -> ${map[from.to?.node]};")
//                    }
//                }
            }
            appendLine("}")
        }
    }

    private fun translateExpr(it: LIRExpr): CFGExpr {
        return when (it) {
            is LIRExpr.LIRConst -> CFGExpr.Const(it.value)
            is LIRMem -> CFGExpr.Mem(translateExpr(it.address))
            is LIRExpr.LIRName -> throw Exception("really not sure about this")
            is LIROp -> CFGExpr.BOp(translateExpr(it.left), translateExpr(it.right), it.op)
            is LIRExpr.LIRTemp -> CFGExpr.Var(it.name)
        }
    }

    private var freshLabelCount = 0
    private fun pointToNext(): Edge.Lazy {
        freshLabelCount++
        pointingTo = "\$G$freshLabelCount"
        return Edge.Lazy(targets, pointingTo)
    }

}