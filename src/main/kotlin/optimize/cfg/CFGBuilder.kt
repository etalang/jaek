package optimize.cfg

import ir.lowered.*

class CFGBuilder(val lir: LIRFuncDecl) {
    val targets: MutableMap<String, CFGNode> = mutableMapOf()
    val head = CFGNode.Start(lir.name, null)
    val nodes: MutableList<CFGNode> = mutableListOf(head)
    fun build(): CFGNode {
        var prev: CFGNode = head
        var next: CFGNode
        var remaining = lir.body.block
        while (remaining.isNotEmpty()) {
            val labels = mutableListOf<String>()
            var first: LIRStmt.FlatStmt = remaining.first()
            while (first is LIRStmt.LIRLabel) {
                labels.add(first.l)
                remaining = remaining.drop(1)
                first = remaining.first()
            }

            next = when (first) {
                is LIRStmt.LIRCJump -> throw Exception("honestly shout out to charles for somehow sneaking a CJUMP in this far after block reordering")
                is LIRStmt.LIRJump -> CFGNode.Cricket(Target.Lazy(targets, first.address.l))
                is LIRReturn -> CFGNode.Return(first.valList.map { translateExpr(it) }, Target.Real(null))
                is LIRStmt.LIRTrueJump -> CFGNode.If(
                    translateExpr(first.guard),
                    Target.Lazy(targets, first.trueBranch.l),
                    null
                )

                is LIRCallStmt -> {
                    // TODO: whole fuckin thing
                    val mooooves = mutableListOf<String>()
                    for (i in 1..first.n_returns.toInt()) {
                        when (val hopefullyMov = remaining.get(i)) {
                            is LIRMove -> {
                                val dest = hopefullyMov.dest
                                if (dest is LIRExpr.LIRTemp) mooooves.add(dest.name)
                                else throw Exception("mooooooving into non-temp")
                            }

                            else -> throw Exception("charles was really hoping this would be a move, and we were too :(")
                        }
                    }
                    //really not sureee if this is ok because it technically drops the lircallstmt and n-1
                    remaining = remaining.drop(first.n_returns.toInt())
                    CFGNode.Funcking(first.target.l, mooooves, first.args.map { translateExpr(it) }, null)
                }

                is LIRStmt.LIRLabel -> throw Exception("charles snuck more labels in")
                is LIRMove -> when (val dest = first.dest) {
                    is LIRExpr.LIRTemp -> {
                        CFGNode.Gets(dest.name, translateExpr(first.expr), null)
                    }

                    is LIRMem -> {
                        CFGNode.Mem(translateExpr(dest), translateExpr(dest), null)
                    }

                    else -> throw Exception("move has a non mem non temp and @kate said that's illegal")
                }
            }

            nodes.add(next)
            labels.forEach { targets[it] = next }

            if (prev.target == null) prev.target = Target.Real(next) //tbh this might only deal with return idfk anymore
            prev = next
            remaining = remaining.drop(1)
        }
        return head
    }

    fun graphViz(): String {
        val map = mutableMapOf<CFGNode, String>()
        nodes.forEachIndexed { index, t -> map[t] = "n$index" }
        return buildString {
            appendLine("digraph ${lir.name} {")
            appendLine("\trankdir=\"TB\"")
            appendLine("\tfontname = \"Helvetica,Arial,sans-serif\";")
            appendLine("\tnode [fontname = \"Helvetica,Arial,sans-serif\";];")
            appendLine("\tedge [fontname = \"Helvetica,Arial,sans-serif\";];")

            map.forEach { appendLine("\t${it.value} [shape=rectangle; label=\"${it.key.pretty}\";];") }
            map.forEach { (from, graphKey) ->

                when (from) {
                    is CFGNode.If -> {
                        if (from.next != null) appendLine("\t$graphKey -> ${map[from.next]} [label=\"T\"];")
                        if (from.take.node != null) appendLine("\t$graphKey -> ${map[from.take.node]} [label=\"F\"];")
                    }

                    is CFGNode.Cricket, is CFGNode.Funcking, is CFGNode.Gets, is CFGNode.Mem, is CFGNode.Return, is CFGNode.Start -> {
                        if (from.next != null) appendLine("\t$graphKey -> ${map[from.next]};")
                    }
                }
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

}