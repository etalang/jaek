package x86

import ir.IRData
import ir.lowered.*
import ir.mid.IRExpr

class Tiler(val IR: LIRCompUnit) {
    private var freshRegisterCount = 0

    private fun freshRegister(): Register {
        freshRegisterCount++
        return Register.Abstract("\$A${freshRegisterCount}")
    }

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
            insns.addAll(tileTree(stmt))
        }
        return insns
    }

    // TODO: IMPLEMENT TILING (HARD)
    private fun tileTree(n : LIRStmt.FlatStmt) : List<Instruction> {
        // pattern matching here directly ugly as hell
        return when (n) {
            is LIRStmt.LIRCJump -> throw Exception("Un-block-reordered IR")
            is LIRStmt.LIRJump -> tileJump(n)
            is LIRStmt.LIRReturn -> tileReturn(n)
            is LIRStmt.LIRTrueJump -> tileTrueJump(n)
            is LIRStmt.LIRCallStmt -> tileCallStmt(n)
            is LIRStmt.LIRLabel -> mutableListOf(Label(n.l))
            is LIRStmt.LIRMove -> tileMove(n)
        }
    }

    // TODO: ADD ALL THE TILES YOU LIKE IN THESE HELPER FUNCTIONS
    private fun tileJump(n : LIRStmt.LIRJump) : List<Instruction> {
        return mutableListOf(Instruction.NOP())
    }

    private fun tileReturn(n : LIRStmt.LIRReturn) : List<Instruction> {
        return mutableListOf(Instruction.NOP())
    }

    private fun tileTrueJump(n : LIRStmt.LIRTrueJump) : List<Instruction> {
        return mutableListOf(Instruction.NOP())
    }

    private fun tileCallStmt(n : LIRStmt.LIRCallStmt) : List<Instruction> {
        return mutableListOf(Instruction.NOP())
    }

    private fun tileMove(n : LIRStmt.LIRMove) : List<Instruction> {
        return mutableListOf(Instruction.NOP())
    }

    /** tileExprSubtree(n) does the heavy lifting to tile expression subtrees */
    private fun tileExprSubtree(n : LIRExpr) : Pair<Register, List<Instruction>> {
        val reg = freshRegister()
        when (n) {
            is LIRExpr.LIRConst -> {
                return Pair(reg,
                    mutableListOf(Instruction.MOV(
                        Destination.RegisterDest(reg), Source.ConstSrc(n.value))))
            }
            is LIRExpr.LIRMem -> { // TODO: recursive call here
                return Pair(reg,
                    mutableListOf(Instruction.MOV(
                        Destination.RegisterDest(reg), Source.ConstSrc(0))))
            }
            is LIRExpr.LIRName -> TODO()
            is LIRExpr.LIROp -> TODO()
            is LIRExpr.LIRTemp -> {
                return Pair(reg,
                    mutableListOf(Instruction.MOV(
                        Destination.RegisterDest(reg), Source.RegisterSrc(Register.Abstract(n.name)))))
            }
        }
    }

}