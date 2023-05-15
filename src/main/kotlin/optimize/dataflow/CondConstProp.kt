//package optimize.dataflow
//
//import edu.cornell.cs.cs4120.etac.ir.IRBinOp.OpType.*
//import ir.optimize.ConstantFolder
//import optimize.cfg.CFG
//import optimize.cfg.CFGExpr
//import optimize.cfg.CFGNode
//import optimize.cfg.Edge
//import optimize.dataflow.Element.*
//import java.io.File
//
//class CondConstProp(cfg: CFG) : CFGFlow.Forward<CondConstProp.Info>(cfg), PostProc {
//    override val top: Info = Info(Unreachability.Top, mutableMapOf())
//    override val name: String = "Conditional Constant Propogation"
//
//    override fun transition(n: CFGNode, inInfo: Info): Map<Edge, Info> {
//        val outInfo = inInfo.copy()
//        val allVarsTop = outInfo.varVals.mapValues { Definition.Top }
//        val unreachableInfo = Info(Unreachability.Top, allVarsTop.toMutableMap())
//        if (n is CFGNode.Start) return n.edges.associateWith { Info(Unreachability.Bottom, inInfo.varVals) } // start must be reachable
//        if (inInfo.unreachability == Unreachability.Bottom) { // if reachable
//            when (n) {
//                is CFGNode.If -> {
//                    when (val guardAbs = abstractInterpretation(n.cond, varVals = outInfo.varVals)) {
//                        Definition.Bottom -> return n.edges.associateWith { outInfo } // can't predict anything here
//                        is Definition.Data -> {
//                            val falseEdge = n.to
//                            val trueEdge = n.take
//                            val cond = n.cond
//                            if (guardAbs.t == 0L) { // false edge TAKEN
//                                if (cond is CFGExpr.BOp && cond.op == NEQ && cond.left is CFGExpr.Var) {
//                                    // add extra info to map based on condition info
//                                    outInfo.varVals[cond.left.name] = abstractInterpretation(cond.right, varVals = outInfo.varVals)
//                                }
//                                falseEdge?.let {
//                                    trueEdge?.let{
//                                        return mapOf(trueEdge to unreachableInfo, falseEdge to outInfo)
//                                    }
//                                    return mapOf(falseEdge to outInfo)
//                                }
//                                trueEdge?.let{return mapOf(trueEdge to unreachableInfo)}
//                                return mapOf()
//                            } else if (guardAbs.t == 1L) { // true edge TAKEN
//                                if (cond is CFGExpr.BOp && cond.op == EQ && cond.left is CFGExpr.Var) {
//                                    // add extra info to map based on condition info
//                                    outInfo.varVals[cond.left.name] = abstractInterpretation(cond.right, varVals = outInfo.varVals)
//                                }
//                                falseEdge?.let {
//                                    trueEdge?.let{
//                                        return mapOf(trueEdge to outInfo, falseEdge to unreachableInfo)
//                                    }
//                                    return mapOf(falseEdge to unreachableInfo)
//                                }
//                                trueEdge?.let{return mapOf(trueEdge to outInfo)}
//                                return mapOf()
//                            } else throw Exception("guard value is neither 0 nor 1, should not typecheck")
//                        }
//                        Definition.Top -> { // this means we have not yet processed node (random ordering!!)
////                            throw Exception("variable in if guard is UNDEFINED, should not typecheck!!")
//                            return n.edges.associateWith { outInfo }
//                        }
//                        is Definition.DesignatedMeeter -> throw Exception("pls do not meet")
//                    }
//                }
//
//                is CFGNode.Gets -> {
//                    outInfo.varVals[n.varName] = abstractInterpretation(n.expr, varVals = outInfo.varVals)
//                    return n.edges.associateWith { outInfo }
//                }
//
//                else -> {
//                    return n.edges.associateWith { outInfo }
//                } // no change
//            }
//
//        } else {
//            return n.edges.associateWith { unreachableInfo } // we return T, T vec when unreachable
//        }
//    }
//
//    fun abstractInterpretation(expr: CFGExpr, varVals: MutableMap<String, Definition>): Definition {
//        // 2+2 =4, 2+ top = top, 2+bot = bot, f(x)= bot
//        // fx doesn't even exist in CFGExpr
//        return when (expr) {
//            is CFGExpr.BOp -> {
//                val leftAbs = abstractInterpretation(expr.left, varVals) // consider whether order matters here
//                val rightAbs = abstractInterpretation(expr.right, varVals)
//                when (leftAbs) {
//                    is Definition.Data -> {
//                        when (rightAbs) {
//                            is Definition.Data -> {
//                                if (expr.op != DIV && rightAbs.t != 0L) {
//                                    Definition.Data(ConstantFolder.calculate(leftAbs.t, rightAbs.t, expr.op))
//                                } else {
//                                    Definition.Bottom // don't do anything and hope for runtime failure
//                                }
//                            }
//
//                            else -> rightAbs
//                        }
//                    }
//
//                    else -> leftAbs
//                }
//            }
//
//            is CFGExpr.Const -> Definition.Data(expr.value)
//            is CFGExpr.Label -> Definition.Bottom // globals, not currently doing this
//            is CFGExpr.Mem -> Definition.Bottom // not currently saving the memory
//            is CFGExpr.Var -> varVals.getOrDefault(expr.name, Definition.Top)
//        }
//    }
//
//    private final val defMoosher = Definition.DesignatedMeeter().meet
//    private final val reachMoosher = Unreachability.DesignatedMeeter().meet
//    override fun meet(e1: Info, e2: Info): Info {
//        val unreachability = reachMoosher.meet(e1.unreachability, e2.unreachability)
//
//        val defdVars = e1.varVals.keys union e2.varVals.keys
//        val e1vals = e1.varVals.withDefault { Definition.Top } // signfies undefd var
//        val e2vals = e2.varVals.withDefault { Definition.Top }
//        val mapMeet = defdVars.map {
//            defMoosher.meet(e1vals.getValue(it), e2vals.getValue(it))
//        }
//        return Info(unreachability, defdVars.zip(mapMeet).toMap().toMutableMap())
//    }
//
//    /** [varVals] must be treated as default T (top) when key not contained */
//    class Info(val unreachability: Unreachability, val varVals: MutableMap<String, Definition>) : EdgeValues() {
//        override val pretty: String get() = "($unreachability, ($varVals))"
//        fun copy() : Info {
//            return Info(unreachability, varVals.toMutableMap())
//        }
//    }
//    override fun postprocess() {
//        removeUnreachables()
//        constantPropogate()
//        deleteConstAssigns()
//    }
//
//    private fun deleteConstAssigns() {
//        var predEdges = cfg.getPredEdges()
//        cfg.getNodes().forEach { curNode ->
//            if (curNode is CFGNode.Gets && curNode.expr is CFGExpr.Const) {
//                curNode.edges.forEach { outEdge -> // a gets should always only have one
//                    val nodePreds = predEdges.getOrDefault(curNode, emptySet())
//                    nodePreds.forEach { inEdge ->
//                        inEdge.node = outEdge.node // delete gets const node
//                    }
//                    predEdges = cfg.getPredEdges()
////                    File("delet${curNode.pretty.filterNot { it.isWhitespace() }}.dot").writeText(graphViz())
//                }
//            }
//        }
//    }
//
//    private fun constantPropogate() {
//        val predEdges = cfg.getPredEdges()
//        cfg.getNodes().forEach { curNode -> // know we have the reachable nodes after remunreach runs
//            val nodePreds = predEdges.getOrDefault(curNode, emptySet())
//            // meet
//            var out: Info? = null
//            nodePreds.forEach {
//                val edgeVal = values[it]!! // every edge should have a value
//                val _out = out
//                out = if (_out == null) edgeVal else meet(_out, edgeVal)
//            }
//            val met =  out ?: top
//            met.varVals.forEach {
//                val value = it.value
//                if (value is Definition.Data) {
//                    when (curNode) {
//                        is CFGNode.Funcking -> {
//                            curNode.args = curNode.args.map { arg ->
//                                replaceVar(arg, it.key, value.t)
//                            }
//                        }
//                        is CFGNode.If -> curNode.cond = replaceVar(curNode.cond, it.key, value.t)
//                        is CFGNode.Gets -> {curNode.expr = replaceVar(curNode.expr, it.key, value.t)
////                            println("for $it replace:${curNode.pretty} with ${replaceVar(curNode.expr, it.key, value.t).pretty}")
////                            println("gets:${curNode.pretty} with expr${curNode.expr.pretty}")
//                        }
//                        is CFGNode.Mem -> {
//                            curNode.loc = replaceVar(curNode.loc, it.key, value.t)
//                            curNode.expr = replaceVar(curNode.expr, it.key, value.t)
//                        }
//                        is CFGNode.Return ->
//                            curNode.rets = curNode.rets.map { ret ->
//                                replaceVar(ret, it.key, value.t)
//                            }
//                        is CFGNode.Start, is CFGNode.Cricket -> {}
//                    }
//                }
//            }
////            print(curNode)
//        }
//    }
//
//    private fun replaceVar(expr : CFGExpr, varName : String, varVal : Long) : CFGExpr {
//        return when (expr) {
//            is CFGExpr.BOp -> CFGExpr.BOp(replaceVar(expr.left, varName, varVal), replaceVar(expr.right, varName, varVal), expr.op)
//            is CFGExpr.Const -> expr
//            is CFGExpr.Label -> expr
//            is CFGExpr.Mem -> CFGExpr.Mem(replaceVar(expr.loc, varName, varVal))
//            is CFGExpr.Var -> if (expr.name == varName) CFGExpr.Const(varVal) else expr
//        }
//    }
//
//    private fun removeUnreachables() {
//        var predEdges =
//            cfg.getPredEdges().toMutableMap() // we could recompute it, but that's quite expensive, so we fix as we go
//        values.forEach {
//            if (it.value.unreachability is Unreachability.Top) {
//                val unreachedEdge = it.key
//                val lastReachedNode = unreachedEdge.from
//                val firstUnreachedNode = unreachedEdge.node
//                when (lastReachedNode) {
//                    is CFGNode.If -> { // two following nodes
//                        val preIfEdges = predEdges.getOrDefault(lastReachedNode, emptySet())
//                        if (unreachedEdge == lastReachedNode.to) {
//                            preIfEdges.forEach {
//                                lastReachedNode.take?.from = it.from
//                                it.from.to = lastReachedNode.take
//                            }
//                        } else { // unreached edge is the take
//                            preIfEdges.forEach {
//                                lastReachedNode.to?.from = it.from
//                                it.from.to = lastReachedNode.to
//                            }
//                        }
//    //                        predEdges[lastReachedNode] = predEdges.getOrDefault(lastReachedNode, emptySet()).plus(preIfEdges)
//                        predEdges = cfg.getPredEdges()
//                            .toMutableMap() // TODO: currently can't fix because would need to predict which "merges" if branches
//                        // hopefully we don't care about our now-abandoned node
//                    }
//
//                    else -> { // one following node
//                        firstUnreachedNode.to?.from = lastReachedNode
//                        lastReachedNode.to = firstUnreachedNode.to // directly mutating
//                    }
//                }
//            }
//        }
//        val validPreds = cfg.getPredEdges() // TODO: delete this!
//        (validPreds.keys intersect predEdges.keys).forEach {
//            require(validPreds[it]!! == predEdges[it]!!)
//        }
//    }
//}