package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRSeq

/** IRSeq represents the sequential composition of IR statements in [block]**/
class LIRSeq(var block: List<FlatStmt>) : LIRStmt() {
    private var freshLabelCount = 0
    override val java: IRSeq = factory.IRSeq(block.map { it.java })

    private fun freshLabel(): String {
        freshLabelCount++
        return "\$B$freshLabelCount"
    }

    fun blockReordering() {
        val b = maximalBasicBlocks()
        val n = buildCFG(b)
        val c = fixJumps(n)
        block = toSequence(c)
    }

    class BasicBlock(
        val label: String, val ordinary: List<FlatStmt>, val end: EndBlock?
    ) {

        class Builder(freshLabel: () -> String) {
            var label: String? = null
            val statements: MutableList<FlatStmt> = ArrayList()
            var end: EndBlock? = null
            fun put(statement: LIRStmt): BasicBlock? {
                when (statement) {
                    is LIRLabel -> {
                        assert(label == null)
                        assert(statements.isEmpty())
                        label = statement.l
                    }

                    is EndBlock -> {
                        assert(end == null)
                        end = statement
                    }

                    is LIRSeq -> throw Exception()
                    is FlatStmt -> statements.add(statement)
                }

                if (end != null) {
                    return build
                }
                return null
            }

            val build: BasicBlock = BasicBlock(label ?: freshLabel.invoke(), statements, end)

        }

    }

    sealed class Collected(
        val statements: MutableList<FlatStmt>,
        val label: String
    ) {
        class DoubleJump(
            statements: MutableList<FlatStmt>,
            label: String,
            val condition: LIRExpr,
            val firstJump: String,
            val fallThroughJump: String
        ) : Collected(statements, label) {
        }
    }

    sealed class Node(
        statements: MutableList<FlatStmt>,
        label: String,
    ) : Collected(statements, label) {
        abstract val edges: List<String>

        class None(statements: MutableList<FlatStmt>, label: String) : Node(statements, label) {
            override val edges: List<String> = listOf()
        }

        class Unconditional(statements: MutableList<FlatStmt>, label: String, val to: String) :
            Node(statements, label) {
            override val edges = listOf(to)
        }

        class Conditional(
            statements: MutableList<FlatStmt>,
            label: String,
            val condition: LIRExpr,
            val trueEdge: String,
            val falseEdge: String?
        ) : Node(statements, label) {
            override val edges: List<String> = listOfNotNull(trueEdge, falseEdge)
        }

    }


    fun maximalBasicBlocks(): List<BasicBlock> {
        val blocks: MutableList<BasicBlock> = ArrayList()
        val statements = block.iterator()
        var builder = BasicBlock.Builder(this::freshLabel)
        while (statements.hasNext()) {
            when (val b = builder.put(statements.next())) {
                is BasicBlock -> {
                    blocks.add(b)
                    builder = BasicBlock.Builder(this::freshLabel)
                }
            }
        }
        return blocks
    }

    fun buildCFG(blocks: List<BasicBlock>): List<Node> {
        return blocks.mapIndexed { index, it ->
            when (val end = it.end) {
                is LIRCJump -> Node.Conditional(
                    ArrayList(it.ordinary), it.label, end.guard, end.trueBranch.l, end.falseBranch?.l
                )

                is LIRTrueJump -> Node.Conditional(
                    ArrayList(it.ordinary), it.label, end.guard, end.trueBranch.l, null
                )

                is LIRJump -> {
                    if (end.address is LIRExpr.LIRName) {
                        Node.Unconditional(ArrayList(it.ordinary), it.label, end.address.l)
                    } else {
                        Node.None(ArrayList(it.ordinary.plus(end)), it.label)
                    }
                }

                is LIRReturn -> Node.None(ArrayList(it.ordinary.plus(end)), it.label)

                null -> Node.Unconditional(ArrayList(it.ordinary), it.label, blocks[index + 1].label)
            }
        }
    }

    fun greedyTrace(nodes: List<Node>): List<Node> {
        val labelToNode = nodes.associateBy { it.label }

        val unmarked: MutableSet<Node> = nodes.toMutableSet()
        val predecessors: Map<Node, MutableList<Node>> = nodes.associateWith { mutableListOf() }
        for (b in nodes) for (children in b.edges) {
            predecessors[labelToNode[children]]?.add(b)
        }

        //TODO: more intelligent selection
        fun head(): Node? {
            for (n in unmarked) if (predecessors[n]?.isEmpty() == true) return n
            return unmarked.randomOrNull()
        }

        val order: MutableList<Node> = ArrayList()
        while (!unmarked.isEmpty()) {
            var head: Node? = head()
            while (head != null) {
                order.add(head)
                unmarked.remove(head)
                //TODO: more intelligent choice of next node
                head = head.edges.filter { unmarked.contains(labelToNode[it]) }.map { labelToNode[it] }.randomOrNull()
            }
        }
        assert(order.containsAll(nodes))
        return order;
    }

    fun fixJumps(nodes: List<Node>): List<Collected> {
        return nodes.mapIndexed { index, node ->
            val nextBlock = nodes.getOrNull(index + 1)
            when (node) {
                is Node.Conditional -> {
                    //CJUMP WITH ONLY TRUE
                    if (node.falseEdge == null) {
                        if (nextBlock != null && node.trueEdge == nextBlock.label) {
                            //GOES IMMEDIATELY TO NEXT BLOCK
                            Node.None(node.statements, node.label)
                        } else {
                            //ONLY TRUE AND CANNOT GO TO NEXT
                            node
                        }
                    } else {
                        //CJUMP WITH TRUE AND FALSE
                        if (nextBlock != null && node.trueEdge == nextBlock.label) {
                            //INVERT CONDITION
                            Node.Conditional(
                                node.statements,
                                node.label,
                                LIRExpr.LIROp(IRBinOp.OpType.XOR, node.condition, LIRExpr.LIRConst(1)),
                                node.falseEdge,
                                null
                            )
                        } else {
                            //FALL THROUGH IS UNCONDITIONAL JUMP
                            Collected.DoubleJump(
                                node.statements,
                                node.label,
                                node.condition,
                                node.trueEdge,
                                node.falseEdge
                            )
                        }
                    }
                }

                is Node.None -> node
                is Node.Unconditional -> {
                    if (nextBlock != null && node.to == nextBlock.label) {
                        Node.None(node.statements, node.label)
                    } else node
                }
            }
        }

    }

    fun toSequence(nodes: List<Collected>): List<FlatStmt> {
        val statements: MutableList<FlatStmt> = ArrayList()
        val blockLabels = nodes.map { it.label }
        nodes.forEach { node ->
            statements.add(LIRLabel(node.label))
            statements.addAll(node.statements)
            when (node) {
                is Node.Conditional -> {
                    assert(node.falseEdge == null)
                    statements.add(LIRTrueJump(node.condition, LIRLabel(node.trueEdge)))
                }

                is Node.None -> {
                }

                is Node.Unconditional -> {
                    statements.add(LIRJump(LIRExpr.LIRName(node.label)))
                }

                is Collected.DoubleJump -> {
                    statements.add(LIRTrueJump(node.condition, LIRLabel(node.firstJump)))
                    statements.add(LIRJump(LIRExpr.LIRName(node.fallThroughJump)))
                }
            }
        }

        return statements

        //TODO: delete labels
//        val labelTargets : MutableSet<LIRLabel> = mutableSetOf()
//        statements.forEach {
//            when(it){
//                is LIRCJump -> throw Exception("???")
//                is LIRJump -> if (it.address is LIRL)
//                is LIRReturn -> TODO()
//                is LIRTrueJump -> TODO()
//                is LIRUJump -> TODO()
//                is LIRCallStmt -> TODO()
//                is LIRLabel -> TODO()
//                is LIRMove -> TODO()
//            }
    }
}