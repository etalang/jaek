package assembly.ralloc

import assembly.x86.Destination
import assembly.x86.Instruction
import assembly.x86.Register
import assembly.x86.Register.*
import assembly.x86.Source
import assembly.ralloc.InterferenceGraph.*

import kotlinx.coroutines.selects.select

class Worklist(val ig : InterferenceGraph, val K : Int, val insns : List<Instruction>) {
    // WORKLISTS/DATA STRUCTURE DECLARATIONS
    val simplifyWorkList = mutableSetOf<Register>() // low-degree nodes, not move-related
    val freezeWorkList = mutableSetOf<Register>() // low-degree nodes, move related
    val spillWorkList = mutableSetOf<Register>() // high-degree nodes

    val spilledNodes = mutableSetOf<Register>() // nodes marked for spilling
    val coalescedNodes = mutableSetOf<Register>() // nodes that have been coalesced
    val coloredNodes = mutableSetOf<Register>() // nodes successfully colored
    val selectStack = mutableListOf<Register>() // stack with temporaries removed from the graph

    /// MOVE INSTRUCTIONS -- the following sets of move insns should be all disjoint from each other
    val worklistMoves = mutableSetOf<Move>()
    val activeMoves = mutableSetOf<Move>()
    val frozenMoves = mutableSetOf<Move>()
    val constrainedMoves = mutableSetOf<Move>()
    val coalescedMoves = mutableSetOf<Move>()

    // DON'T COLOR THINGS RSP OR RBP COLOR!
    val reservedColors = setOf(4,5)

    init {
        for (reg in ig.degrees.keys) {
            if (reg is Abstract) {
                if (ig.degrees[reg]!! >= K) {
                    spillWorkList.add(reg)
                }
                else if (moveRelated(reg)) {
                    freezeWorkList.add(reg)
                }
                else {
                    simplifyWorkList.add(reg)
                }
            }

        }
        //// INITIALIZE MOVE INSTRUCTION WORKLIST
        // unless workListMoves is like ordered it's fine
        for (insn in insns) {
            if (insn is Instruction.MOV)
                if (insn.dest is Destination.RegisterDest && insn.src is Source.RegisterSrc
                    && !(insn.dest.r is x86 && insn.src.r is x86))
                    worklistMoves.add(Move(insn.dest.r, insn.src.r))
        }
    }

    /* HELPERS FOR ANALYZING NODES */

    fun nodeMoves(n : Register) : Set<Move> {
        return ig.moveList[n]?.intersect(activeMoves union worklistMoves) ?: emptySet()
    }
    fun moveRelated(n : Register) : Boolean {
        return nodeMoves(n).isNotEmpty()
    }

    fun adjacent(n : Register) : MutableSet<Register> {
        return ig.adjList[n]?.minus(selectStack.toSet() union coalescedNodes)?.toMutableSet() ?: mutableSetOf()
    }

    /* REQUIRED FOR SIMPLIFY */
    fun decrementDegree(m : Register) {
        val d = ig.degrees[m]
        ig.degrees[m] = d?.minus(1) ?: 0
        if (d != null && d == K) {
            val neighborhood = adjacent(m)
            neighborhood.add(m)
            enableMoves(neighborhood)
            spillWorkList.remove(m)
            if (moveRelated(m)) {
                freezeWorkList.add(m)
            }
            else {
                simplifyWorkList.add(m)
            }
        }
    }

    fun enableMoves(nodes : Set<Register>) {
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
    fun addWorkList(u : Register) {
        if (u is Abstract && !(moveRelated(u))) {
            val du = ig.degrees[u]
            if (du != null && du < K) {
                freezeWorkList.remove(u)
                simplifyWorkList.add(u)
            }
        }
    }

    fun OK(t : Register, r : Register) : Boolean {
        if (t is x86) return true
        else {
            val dt = ig.degrees[t]
            val tNeighbors = ig.adjList[t]
            if (dt != null && dt < K) return true
            return (tNeighbors?.contains(r) == true)
        }
    }

    fun conservative(nodes : Set<Register>) : Boolean {
        var k = 0
        for (n in nodes) {
            val dn = ig.degrees[n]
            if (dn != null && dn >= K) k++
        }
        return k < K
    }

    fun getAlias(n : Register) : Register {
        return if (coalescedNodes.contains(n)) {
            val identified = ig.alias[n]
            if (identified != null) getAlias(identified) else n
        }
        else n
    }

    fun combine(u : Register, v : Register) {
        if (freezeWorkList.contains(v)) {
            freezeWorkList.remove(v)
        }
        else {
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
    fun freezeMoves(u : Register) {
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
        while (selectStack.isNotEmpty()) {
            val n = selectStack.removeLast()
//            selectStack = selectStack.removeLast
            if (n is x86) {
                coloredNodes.add(n)
                continue
            }
            val okColors = mutableSetOf<Int>()
            for (idx in 0 until K) {
                if (!reservedColors.contains(idx)) {
                    okColors.add(idx)
                }
            }
//            okColors.addAll(0 until K)
            val nNeighbors = ig.adjList[n] ?: emptySet()
            for (w in nNeighbors) {
                val r = getAlias(w)
                if (coloredNodes.contains(r) || r is x86) {
                    ig.colors[r].let{ okColors.remove(it) }
                }
            }
            if (okColors.isEmpty()) {
                spilledNodes.add(n)
            }
            else {
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