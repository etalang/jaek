package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRSeq

/** IRSeq represents the sequential composition of IR statements in [block]**/
class LIRSeq(val block: List<FlatStmt>) : LIRStmt() {
    override val java: IRSeq = factory.IRSeq(block.map { it.java })

    sealed class Node(
        val statements: MutableList<FlatStmt>,
        val label: String?,
    ) {
        abstract val edges: List<Node?>

        class None(statements: MutableList<FlatStmt>, label: String?) : Node(statements, label) {
            override val edges: List<Node> = listOf()
        }

        class Unconditional(statements: MutableList<FlatStmt>, label: String?, var unconEdge: Node?) :
            Node(statements, label) {
            override val edges = listOf(unconEdge)

        }

        class Conditional(
            statements: MutableList<FlatStmt>, label: String?, var trueEdge: Node?, var falseEdge: Node?
        ) : Node(statements, label) {
            override val edges: List<Node?> = listOf(trueEdge, falseEdge)
        }

        sealed class Builder(
            val statements: MutableList<FlatStmt>,
            val block: BasicBlock?,
        ) {
            var built: Node? = null

            abstract fun build()
            abstract fun finish(map: Map<BasicBlock?, Node?>): Node

            class None(statements: MutableList<FlatStmt>, block: BasicBlock) : Builder(statements, block) {
                override fun build() {
                    built = Node.None(statements, block?.label?.l)
                }

                override fun finish(map: Map<BasicBlock?, Node?>): Node {
                    return built!!;
                }
            }

            class Unconditional(statements: MutableList<FlatStmt>, block: BasicBlock, var lazyUncon: BasicBlock) :
                Builder(statements, block) {
                override fun build() {
                    built = Node.Unconditional(statements, block?.label?.l, null)
                }

                override fun finish(map: Map<BasicBlock?, Node?>): Node {
                    (built as Node.Unconditional).unconEdge = map[lazyUncon]
                    return built!!;
                }

            }

            class Conditional(
                statements: MutableList<FlatStmt>,
                label: BasicBlock,
                var guard: LIRExpr,
                var lazyTrue: BasicBlock?,
                var lazyFalse: BasicBlock?
            ) : Builder(statements, label) {
                override fun build() {
                    built = Node.Conditional(statements, block?.label?.l, null, null)
                }

                override fun finish(map: Map<BasicBlock?, Node?>): Node {
                    (built as Node.Conditional).trueEdge = map[lazyTrue]
                    (built as Node.Conditional).falseEdge = map[lazyFalse]
                    return built!!;
                }
            }
        }


    }

    class BasicBlock(
        val label: LIRLabel?, val ordinary: List<FlatStmt>, val end: EndBlock?
    ) {

        class Builder() {
            var label: LIRLabel? = null
            val statements: MutableList<FlatStmt> = ArrayList()
            var end: EndBlock? = null
            fun put(statement: LIRStmt): BasicBlock? {
                when (statement) {
                    is LIRLabel -> {
                        assert(label == null)
                        assert(statements.isEmpty())
                        label = statement
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

            val build: BasicBlock = BasicBlock(label, statements, end)

        }

    }


    fun maximalBasicBlocks(): List<BasicBlock> {
        val blocks: MutableList<BasicBlock> = ArrayList()
        val statements = block.iterator()
        var builder = BasicBlock.Builder()
        while (statements.hasNext()) {
            when (val b = builder.put(statements.next())) {
                is BasicBlock -> {
                    blocks.add(b)
                    builder = BasicBlock.Builder()
                }
            }
        }
        return blocks
    }

    //this caused me pain
    fun buildCFG(blocks: List<BasicBlock>): List<Node> {
//        val nodeMap = blocks.associateWith { Node(ArrayList(it.ordinary),it.label) }
        val labelToBlock = blocks.filter { it.label != null }.associateBy { it.label }
//        val nodes: MutableList<Node> = ArrayList()


        val lazyNodes = blocks.mapIndexed { index, basicBlock ->
            when (val end = basicBlock.end) {
                is LIRCJump -> {
                    Node.Builder.Conditional(
                        ArrayList(basicBlock.ordinary),
                        basicBlock,
                        end.guard,
                        labelToBlock[end.trueBranch],
                        labelToBlock[end.falseBranch]
                    )
                }

                is LIRJump, is LIRReturn -> {
                    Node.Builder.None(
                        ArrayList(basicBlock.ordinary.plus(end)),
                        basicBlock,
                    )
                }

                is LIRTrueJump -> {
                    //TODO consider more thoughtfully
                    throw Exception("how did you get here")
//                    n.trueEdge = labelToNode[end.trueBranch]
//                    n.statements.add(end)
                }

                null -> Node.Builder.Unconditional(
                    ArrayList(basicBlock.ordinary), basicBlock, blocks[index + 1]
                )
            }
        }
        for (n in lazyNodes) n.build()

        val labelToNode = lazyNodes.associate { it.block to it.built }
        return lazyNodes.map { it.finish(labelToNode) }
    }


    fun greedyTrace(blocks: List<Node>): List<Node> {
        val unmarked: MutableSet<Node> = blocks.toMutableSet()
        val predecessors: Map<Node, MutableList<Node>> = blocks.associateWith { mutableListOf() }
        for (b in blocks) for (children in b.edges) {
            predecessors[children]?.add(b)
        }

        fun head(): Node? {
            for (n in unmarked) {
                if (predecessors[n]?.isEmpty() == true) return n;
            }
            return unmarked.randomOrNull()
        }

        val order: MutableList<Node> = ArrayList()
        while (!unmarked.isEmpty()) {
            var head: Node? = head()
            while (head != null) {
                order.add(head)
                unmarked.remove(head)
                head = head.edges.filter { unmarked.contains(it) }.randomOrNull()
            }
        }
        assert(order.containsAll(blocks))
        return order;
    }

    fun fixJumps(nodes: List<Node>): List<FlatStmt> {
        val fixed: List<BasicBlock> = nodes.mapIndexed { index, node ->
            val statements = ArrayList(node.statements)
            var jump: FlatStmt? = null;
//            //TODO: dangerous LMAO
//            if (index < nodes.size - 1 && node.trueEdge != null && node.trueEdge?.label == nodes[index + 1].label) {
//                jump = LIRTrueJump(node.condition!!, node.trueEdge!!.label!!)
//            }
//            if (!(node.unconEdge?.label == nodes[index + 1].label)) {
//                jump = LIRJump(node.condition!!)
//            }

            BasicBlock(null, if (jump != null) node.statements.plus(jump) else node.statements, null)
        }

        return
    }


}