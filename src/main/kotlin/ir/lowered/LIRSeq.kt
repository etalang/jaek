package ir.lowered

import assembly.RegisterAllocator
import assembly.Tile
import assembly.TileBuilder
import edu.cornell.cs.cs4120.etac.ir.IRBinOp
import edu.cornell.cs.cs4120.etac.ir.IRSeq

/** IRSeq represents the sequential composition of IR statements in [block]**/
class LIRSeq(var block: List<FlatStmt>) : LIRStmt() {
    override val java: IRSeq get() = factory.IRSeq(block.map { it.java })

    override val defaultTile: Tile.Regular
        get() {
            // TODO!!!! WE SHOULD USE BUILDER HERE!!!!!!!!! @blu

            val builder = TileBuilder.Regular(0, this)
            for (stmt in block) builder.consume(stmt.optimalTile())

            // TODO: do register allocation here
            // TODO: add preamble (currently a full guess)
            val ra = RegisterAllocator()
            val insns = ra.allocateRegisters(builder.publicIns)
            return Tile.Regular(insns, builder.publicCost)
//            return builder.build()
        }

    override fun findBestTile() {}

    fun blockReordering(freshLabel: () -> String): LIRSeq {
        val b = maximalBasicBlocks(freshLabel)
        val n = buildCFG(b)
        val g = greedyTrace(n)
        val j = fixJumps(g, freshLabel)
        val c = removeUselessJumps(j)
        val s = LIRSeq(toSequence(c))
        return s
    }

    class BasicBlock(
        val label: String, val ordinary: List<FlatStmt>, val end: EndBlock?
    ) {
        class Builder(val freshLabel: () -> String) {
            var label: String? = null
            private val statements: MutableList<FlatStmt> = ArrayList()
            var end: EndBlock? = null
            fun put(statement: LIRStmt) {
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

            }

            val complete: Boolean get() = end != null

            val build: BasicBlock
                get() = BasicBlock(label ?: freshLabel(), statements, end)

        }

        override fun toString(): String {
            return "BasicBlock(label='$label', ordinary=$ordinary, end=$end)"
        }

    }

//
//        class DoubleJump(
//            statements: MutableList<FlatStmt>,
//            label: String,
//            val condition: LIRExpr,
//            val firstJump: String,
//            val fallThroughJump: String
//        ) : Collected(statements, label) {
//        }


    sealed class Node(
        val statements: List<FlatStmt>,
        val label: String
    ) {
        abstract val edges: List<String>

        class None(statements: List<FlatStmt>, label: String) : Node(statements, label) {
            override val edges: List<String> = listOf()
        }

        class Unconditional(statements: List<FlatStmt>, label: String, val to: String) :
            Node(statements, label) {
            override val edges = listOf(to)
        }

        class Conditional(
            statements: List<FlatStmt>,
            label: String,
            val condition: LIRExpr,
            val trueEdge: String,
            val falseEdge: String?
        ) : Node(statements, label) {
            override val edges: List<String> = listOfNotNull(trueEdge, falseEdge)
        }

        override fun toString(): String {
            return "Node(label='$label', statements=$statements, edges=$edges)"
        }

    }


    private fun maximalBasicBlocks(freshLabel: () -> String): List<BasicBlock> {
        val blocks: MutableList<BasicBlock> = ArrayList()
        val statements = block.iterator()
        var builder = BasicBlock.Builder(freshLabel)
        while (statements.hasNext()) {
            val next = statements.next()
            if (next is LIRLabel || builder.complete) {
                blocks.add(builder.build)
                builder = BasicBlock.Builder(freshLabel)
            }
            builder.put(next)
        }
        blocks.add(builder.build)
        return blocks
    }

    private fun buildCFG(blocks: List<BasicBlock>): List<Node> {
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

                null -> {
                    if (index < blocks.lastIndex)
                        Node.Unconditional(ArrayList(it.ordinary), it.label, blocks[index + 1].label)
                    else
                        Node.None(ArrayList(it.ordinary), it.label)
                }
            }
        }
    }

    /** returns a topological sort of the CFG */
    private fun greedyTrace(nodes: List<Node>): List<Node> {
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
        return order
    }

    private fun fixJumps(nodes: List<Node>, freshLabel: () -> String): List<Node> {
        //TODO: think hard about this nested list approach... alternative approach was DoubleJump (see earlier history)
        return nodes.mapIndexed { index, node ->
            val nextBlock = nodes.getOrNull(index + 1)
            when (node) {
                is Node.Conditional -> {
                    //CJUMP WITH ONLY TRUE
                    // unsure this is correct at all -- there needs to be some jump no matter what
                    if (node.falseEdge == null) {
                        if (nextBlock != null && node.trueEdge == nextBlock.label) {
                            //GOES IMMEDIATELY TO NEXT BLOCK
                            listOf(Node.None(node.statements, node.label))
                        } else {
                            //ONLY TRUE AND CANNOT GO TO NEXT
                            listOf(node)
                        }
                    } else {
                        //CJUMP WITH TRUE AND FALSE
                        if (nextBlock != null && node.trueEdge == nextBlock.label) {
                            //INVERT CONDITION
                            // label needed here?
                            listOf(
                                Node.Conditional(
                                    node.statements,
                                    node.label,
                                    LIROp(IRBinOp.OpType.XOR, node.condition, LIRExpr.LIRConst(1)),
                                    node.falseEdge,
                                    null
                                )
                            )
                        } else {
                            //FALL THROUGH IS UNCONDITIONAL JUMP
                            listOf(
                                Node.Conditional(
                                    node.statements,
                                    node.label,
                                    node.condition,
                                    node.trueEdge,
                                    null
                                ),
                                Node.Unconditional(ArrayList(), freshLabel(), node.falseEdge)
                            )
                        }
                    }
                }

                is Node.None -> listOf(node)
                is Node.Unconditional -> {
                    listOf(node)
                }
            }
        }.flatten()
    }

    private fun removeUselessJumps(nodes: List<Node>): List<Node> {
        return nodes.mapIndexed { index, node ->
            val nextBlock = nodes.getOrNull(index + 1)
            when (node) {
                is Node.Conditional -> node
                is Node.None -> node
                is Node.Unconditional -> {
                    if (nextBlock != null && node.to == nextBlock.label) Node.None(node.statements, node.label)
                    else node
                }
            }
        }
    }


    private fun toSequence(nodes: List<Node>): List<FlatStmt> {
        val statements: MutableList<FlatStmt> = ArrayList()
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
                    statements.add(LIRJump(LIRExpr.LIRName(node.to)))
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