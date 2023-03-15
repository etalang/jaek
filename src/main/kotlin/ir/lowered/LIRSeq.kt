package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRSeq

/** IRSeq represents the sequential composition of IR statements in [block]**/
class LIRSeq(val block: List<FlatStmt>) : LIRStmt() {
    private var freshLabelCount = 0
    override val java: IRSeq = factory.IRSeq(block.map { it.java })

    private fun freshLabel(): LIRLabel {
        freshLabelCount++
        return LIRLabel("\$B$freshLabelCount")
    }

    class BasicBlock(
        val label: LIRLabel, val ordinary: List<FlatStmt>, val end: EndBlock?
    ) {

        class Builder(freshLabel: () -> LIRLabel) {
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

            val build: BasicBlock = BasicBlock(label ?: freshLabel.invoke(), statements, end)

        }

    }

    sealed class Node(
        val statements: MutableList<FlatStmt>,
        val label: LIRLabel,
    ) {
        abstract val edges: List<LIRLabel>

        class None(statements: MutableList<FlatStmt>, label: LIRLabel) : Node(statements, label) {
            override val edges: List<LIRLabel> = listOf()
        }

        class Unconditional(statements: MutableList<FlatStmt>, label: LIRLabel, val to: LIRLabel) :
            Node(statements, label) {
            override val edges = listOf(to)
        }

        class Conditional(
            statements: MutableList<FlatStmt>,
            label: LIRLabel,
            val condition: LIRExpr,
            val trueEdge: LIRLabel,
            val falseEdge: LIRLabel?
        ) : Node(statements, label) {
            override val edges: List<LIRLabel> = listOfNotNull(trueEdge, falseEdge)
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
                    ArrayList(it.ordinary), it.label, end.guard, end.trueBranch, end.falseBranch
                )

                is LIRTrueJump -> Node.Conditional(
                    ArrayList(it.ordinary), it.label, end.guard, end.trueBranch, null
                )

                is LIRJump, is LIRReturn -> Node.None(ArrayList(it.ordinary.plus(end)), it.label)

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

    fun fixJumps(nodes: List<Node>): List<FlatStmt> {
        val fixed: List<BasicBlock> = nodes.mapIndexed { index, node ->
            val statements = ArrayList(node.statements)
            when (node) {
                is Node.Conditional -> TODO()
                is Node.None -> TODO()
                is Node.Unconditional -> TODO()
            }
        }
        return listOf()
    }
}


//            //TODO: dangerous LMAO
//            if (index < nodes.size - 1 && node.trueEdge != null && node.trueEdge?.label == nodes[index + 1].label) {
//                jump = LIRTrueJump(node.condition!!, node.trueEdge!!.label!!)
//            }
//            if (!(node.unconEdge?.label == nodes[index + 1].label)) {
//                jump = LIRJump(node.condition!!)
//            }

//            BasicBlock(null, if (jump != null) node.statements.plus(jump) else node.statements, null)
//}

//        return

//        sealed class Builder(
//            val statements: MutableList<FlatStmt>,
//            val block: BasicBlock?,
//        ) {
//            var built: Node? = null
//
//            abstract fun build()
//            abstract fun finish(map: Map<BasicBlock?, Node?>): Node
//
//            class None(statements: MutableList<FlatStmt>, block: BasicBlock) : Builder(statements, block) {
//                override fun build() {
//                    built = Node.None(statements, block?.label?.l)
//                }
//
//                override fun finish(map: Map<BasicBlock?, Node?>): Node {
//                    return built!!;
//                }
//            }
//
//            class Unconditional(statements: MutableList<FlatStmt>, block: BasicBlock, var lazyUncon: BasicBlock) :
//                Builder(statements, block) {
//                override fun build() {
//                    built = Node.Unconditional(statements, block?.label?.l, null)
//                }
//
//                override fun finish(map: Map<BasicBlock?, Node?>): Node {
//                    (built as Node.Unconditional).unconEdge = map[lazyUncon]
//                    return built!!;
//                }
//
//            }

//            class Conditional(
//                statements: MutableList<FlatStmt>,
//                label: BasicBlock,
//                var guard: LIRExpr,
//                var lazyTrue: BasicBlock?,
//                var lazyFalse: BasicBlock?
//            ) : Builder(statements, label) {
//                override fun build() {
//                    built = Node.Conditional(statements, block?.label?.l, null, null)
//                }
//
//                override fun finish(map: Map<BasicBlock?, Node?>): Node {
//                    (built as Node.Conditional).trueEdge = map[lazyTrue]
//                    (built as Node.Conditional).falseEdge = map[lazyFalse]
//                    return built!!;
//                }
//            }
//        }