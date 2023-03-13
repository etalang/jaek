package ir.lowered

import edu.cornell.cs.cs4120.etac.ir.IRSeq

/** IRSeq represents the sequential composition of IR statements in [block]**/
class LIRSeq(val block: List<FlatStmt>) : LIRStmt() {
    override val java: IRSeq = factory.IRSeq(block.map { it.java })

    class BasicBlock(
        val label: LIRLabel?, val ordinary: List<FlatStmt>, val end: EndBlock?
    ) {
        class Node(
            val statements: MutableList<FlatStmt>,
            var trueEdge: Node?,
            var falseEdge: Node?,
            var unconEdge: Node?
        ) {
            constructor(statements: MutableList<FlatStmt>) :
                    this(statements, null, null, null)

            val edges = listOfNotNull(trueEdge, falseEdge, unconEdge)
        }

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
}