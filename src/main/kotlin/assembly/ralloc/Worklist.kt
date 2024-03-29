package assembly.ralloc

import assembly.ralloc.InterferenceGraph.*
import assembly.x86.Destination
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.Register.*
import assembly.x86.Source
import java.util.*

class Worklist(val ig: InterferenceGraph, val K: Int,  insns: List<Instruction>, m: Set<Move>) {
    // WORKLISTS/DATA STRUCTURE DECLARATIONS
    val initial = mutableSetOf<Abstract>()

    val simplifyWorkList = mutableSetOf<Register>() // low-degree nodes, not move-related
    val freezeWorkList = mutableSetOf<Register>() // low-degree nodes, move related
    val spillWorkList = mutableSetOf<Register>() // high-degree nodes

    val spilledNodes = mutableSetOf<Register>() // nodes marked for spilling
    val coalescedNodes = mutableSetOf<Register>() // nodes that have been coalesced
    val coloredNodes = mutableSetOf<Register>() // nodes successfully colored
    val selectStack = Stack<Register>() // stack with temporaries removed from the graph

    /// MOVE INSTRUCTIONS -- the following sets of move insns should be all disjoint from each other
    val coalescedMoves = mutableSetOf<Move>()
    val constrainedMoves = mutableSetOf<Move>()
    val worklistMoves = m.toMutableSet()
    val frozenMoves = mutableSetOf<Move>()
    val activeMoves = mutableSetOf<Move>()

    // DON'T COLOR THINGS RSP OR RBP COLOR!
//    val reservedColors = setOf(0, 3) // RAX and RDX


    init {
        val initial = insns.flatMap { it.involved }.toMutableSet()
        while (initial.isNotEmpty()) {
            val reg = initial.elementAt(0)
            initial.remove(reg)

            if (ig.degrees[reg]!! >= K) {
                spillWorkList.add(reg)
            } else if (moveRelated(reg)) {
                freezeWorkList.add(reg)
            } else {
                simplifyWorkList.add(reg)
            }
        }

        //// INITIALIZE MOVE INSTRUCTION WORKLIST
        // unless workListMoves is ordered it's fine
        for (insn in insns) {
            if (insn is Instruction.MOV)
                if (insn.dest is Destination.RegisterDest && insn.src is Source.RegisterSrc
                    && !(insn.dest.r is x86 && insn.src.r is x86)
                )
                    worklistMoves.add(Move(insn.dest.r, insn.src.r))
        }
    }

    /* HELPERS FOR ANALYZING NODES */

    fun nodeMoves(n: Register): Set<Move> {
        return ig.moveList.getOrDefault(n, emptySet()).intersect(activeMoves union worklistMoves)
    }

    fun moveRelated(n: Register): Boolean {
        return nodeMoves(n).isNotEmpty()
    }

    fun adjacent(n: Register): MutableSet<Register> {
        return ig.adjList[n]!!.minus(selectStack.toSet() union coalescedNodes).toMutableSet()
    }

    /* REQUIRED FOR SIMPLIFY */
    fun decrementDegree(m: Abstract) {
        val d = ig.degrees[m]
        ig.degrees[m] = d!!.minus(1)
        if (d == K) {
            enableMoves(setOf(m) union adjacent(m))
            spillWorkList.remove(m)
            if (moveRelated(m)) {
                freezeWorkList.add(m)
            } else {
                simplifyWorkList.add(m)
            }
        }
    }

    fun enableMoves(nodes: Set<Register>) {
        for (n in nodes) {
            for (m in nodeMoves(n)) {
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m)
                    worklistMoves.add(m)
                }
            }
        }
    }

    /* REQUIRED FOR COALESCE */
    fun addWorkList(u: Register) {
        if (u !is x86 && !(moveRelated(u)) && ig.degrees[u]!! < K) {
            freezeWorkList.remove(u)
            simplifyWorkList.add(u)
        }
    }

    fun OK(t: Register, r: Register): Boolean {
        return t is x86 || ig.degrees[t]!! < K || Pair(t, r) in ig.adjSet
//        if (t is x86) return true
//        else {
//            val dt = ig.degrees[t]
//            if (dt != null && dt < K) return true
//            return (Pair(t,r) in ig.adjSet)
//        }
    }

    fun conservative(nodes: Set<Register>): Boolean {
        var k = 0
        for (n in nodes) {
            if (n is x86 || ig.degrees[n]!! >= K) k++
        }
        return k < K
    }

    fun getAlias(n: Register): Register {
        return if (coalescedNodes.contains(n)) {
            getAlias(ig.alias[n]!!)
        } else n
    }

    fun combine(u: Register, v: Register) {
        if (freezeWorkList.contains(v)) {
            freezeWorkList.remove(v)
        } else {
            spillWorkList.remove(v)
        }
        coalescedNodes.add(v)
        ig.alias[v] = u
        val uMoves = ig.moveList[u] ?: emptySet()
        val vMoves = ig.moveList[v] ?: emptySet()
        ig.moveList[u] = (uMoves.union(vMoves)).toMutableSet()
        enableMoves(setOf(v))
        val neighbors = adjacent(v)
        for (t in neighbors) {
            if (t is Abstract) {
                ig.addEdge(t, u)
                decrementDegree(t)
            }
        }
        val du = ig.degrees[u]
        if (du != null && du >= K && freezeWorkList.contains(u)) {
            freezeWorkList.remove(u)
            spillWorkList.add(u)
        }
    }

    /* REQUIRED FOR FREEZE */
    fun freezeMoves(u: Register) {
        for (m in nodeMoves(u)) {
            val x = m.dest
            val y = m.src
            val v = (if (getAlias(y) == getAlias(u)) getAlias(x) else getAlias(y))
            activeMoves.remove(m)
            frozenMoves.add(m)
            if (freezeWorkList.contains(v) && nodeMoves(v).isEmpty()) {
                freezeWorkList.remove(v)
                simplifyWorkList.add(v)
            }
        }
    }

    fun assignColors() {
        for (n in Register.x86Name.values()) {
            ig.colors[x86(n)] = n.ordinal
        }
        while (selectStack.isNotEmpty()) {
            val n = selectStack.pop()
            if (n is x86) {
                coloredNodes.add(n)
                continue
            }
            val okColors = mutableSetOf<Int>()
            for (idx in 0 until K) {
//                if (!reservedColors.contains(idx)) {
                    okColors.add(idx)
//                }
            }
            for (w in ig.adjList[n]!!) {
                val r = getAlias(w)
                if (coloredNodes.contains(r) || r is x86) {
                    okColors.remove(ig.colors[r])
                }
            }
            if (okColors.isEmpty()) {
                spilledNodes.add(n)
            } else {
                coloredNodes.add(n)
                val c = okColors.elementAt(0)
                ig.colors[n] = c
            }
        }
        for (n in coalescedNodes) {
            ig.colors[n] = ig.colors[getAlias(n)] ?: -1  // don't know what the default should be?
        }
    }


}