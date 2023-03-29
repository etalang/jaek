package x86

import ir.IRData
import ir.lowered.*

class Tiler(val IR: LIRCompUnit) {

    fun tile() : x86CompUnit {
        return tileCompUnit(IR)
    }

    private fun tileCompUnit(n : LIRCompUnit) : x86CompUnit {
        val assemblyFuncs : MutableList<x86FuncDecl> = ArrayList()
        n.functions.forEach { assemblyFuncs.add(tileFuncDecl(it)) }
        val assemblyData : MutableList<x86Data> = ArrayList()
        n.globals.forEach { assemblyData.add(tileData(it)) }
        return x86CompUnit(n.name, assemblyFuncs, assemblyData)
    }

    private fun tileData(n : IRData) : x86Data {
        return x86Data(n.name, n.data)
    }

    private fun tileFuncDecl(n : LIRFuncDecl) : x86FuncDecl {
        val insnBlock : List<Instruction> = tileSeq(n.body)
        return x86FuncDecl(n.name, insnBlock)
    }

    private fun tileSeq(n : LIRSeq) : List<Instruction> {
        var insns = mutableListOf<Instruction>()
        for (stmt in n.block) {
            insns.addAll(tileStatement(stmt))
        }
        return insns
    }

    // TODO: IMPLEMENT TILING
    private fun tileStatement(n : LIRStmt) : List<Instruction> {
        return mutableListOf(Instruction.NOP)
    }

    private fun tileExpr(n : LIRExpr) : List<Instruction> {
        return mutableListOf(Instruction.NOP)
    }

}