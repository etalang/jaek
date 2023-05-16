package optimize.cfg

import ir.lowered.*
import java.util.*

class CFGBuilder(val lir: LIRFuncDecl) {
    private val targets: MutableMap<String, CFGNode> = mutableMapOf()
    private val nodes: MutableList<CFGNode>
    private var pointingTo: String? = null
    val start: CFGNode.Start
    private val matchMaker : MatchMaker
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
                    CFGNode.Cricket(currStmt.address.l)
                }

                is LIRReturn -> {
                    pointingTo = null
                    CFGNode.Return(currStmt.valList.map { translateExpr(it) } )
                }

                is LIRStmt.LIRTrueJump -> CFGNode.If(
                    translateExpr(currStmt.guard), currStmt.trueBranch.l, pointToNext()
                )

                is LIRCallStmt -> {
                    val returnMoves = mutableListOf<CFGNode.Mov>()
                    for (i in 0 until currStmt.n_returns.toInt()) {
                        when (val movLIR = statements.removeFirst()) {
                            is LIRMove -> returnMoves.add(translateMove(movLIR))
                            else -> throw Exception("charles was really hoping this would be a move, and we were too :(")
                        }
                    }
                    CFGNode.Funcking(
                        currStmt.target.l, returnMoves, currStmt.args.map { translateExpr(it) }, pointToNext()
                    )
                }

                is LIRStmt.LIRLabel -> throw Exception("charles snuck more labels in")
                is LIRMove -> translateMove(currStmt)
            }

            nodes.add(next)
            labels.forEach {
                val target = targets[it]
                if (target == null) targets[it] = next
            }
        }
        matchMaker = MatchMaker(start,targets)
        nodes.forEach { it.resolveEdges(matchMaker) }
    }

    fun build(): CFG {
        return CFG(start, lir.name, matchMaker)
    }

    private fun translateMove(currStmt: LIRMove): CFGNode.Mov {
        return when (val dest = currStmt.dest) {
            is LIRExpr.LIRTemp -> {
                CFGNode.Gets(dest.name, translateExpr(currStmt.expr), pointToNext())
            }

            is LIRMem -> {
                CFGNode.Mem(translateExpr(dest), translateExpr(currStmt.expr), pointToNext())
            }

            else -> throw Exception("move has a non mem non temp and @kate said that's illegal")
        }
    }

    private fun translateExpr(it: LIRExpr): CFGExpr {
        return when (it) {
            is LIRExpr.LIRConst -> CFGExpr.Const(it.value)
            is LIRMem -> CFGExpr.Mem(translateExpr(it.address))
            is LIRExpr.LIRName -> CFGExpr.Label(it.l)
            is LIROp -> CFGExpr.BOp(translateExpr(it.left), translateExpr(it.right), it.op)
            is LIRExpr.LIRTemp -> CFGExpr.Var(it.name)
        }
    }

    private var freshLabelCount = 0
    private fun pointToNext(): String {
        freshLabelCount++
        pointingTo = "\$G$freshLabelCount"
        return pointingTo!!
    }

}