package ir

import ast.*
import ir.mid.*
import ir.mid.IRExpr.IRTemp
import edu.cornell.cs.cs4120.etac.ir.IRNode as JIRNode

class IRTranslator(val AST: Program, val name:String) {
    fun irgen(optimize: Boolean = false): JIRNode {
        return translateCompUnit(AST).java
    }

    private fun translateCompUnit(p: Program): IRCompUnit {
        val globals: MutableList<IRData> = ArrayList()
        val functions: MutableList<IRFuncDecl> = ArrayList()

        p.definitions.forEach {
            when (it) {
                is GlobalDecl -> if (it.value != null) globals.add(translateData(it))
                is Method -> if (it.body != null) functions.add(translateFuncDecl(it))
            }
        }
        return IRCompUnit(name, functions, globals)
    }

    private fun translateData(n: GlobalDecl): IRData {
        val data: LongArray = when (val v = n.value!!) { //hehe non null!!
            is Literal.ArrayLit -> TODO()
            is Literal.BoolLit -> longArrayOf(if (v.bool) 1 else 0)
            is Literal.CharLit -> longArrayOf(v.char.toLong())
            is Literal.IntLit -> longArrayOf(v.num)
            is Literal.StringLit -> TODO()
        }
        return IRData(n.id, data)
    }

    private fun translateFuncDecl(n: Method): IRFuncDecl {
        return IRFuncDecl(n.id, translateStatement(n.body!!)) //WOOHOOOOOOO lol
    }

    private fun translateStatement(n: Node): IRStmt {
        return when (n) {
            is AssignTarget.ArrayAssign -> TODO()
            is AssignTarget.DeclAssign -> TODO()
            is AssignTarget.IdAssign -> TODO()
            is AssignTarget.Underscore -> TODO()
            is GlobalDecl -> TODO()
            is Method -> TODO()
            is Interface -> TODO()
            is Program -> TODO()
            is Statement.ArrayInit -> TODO()
            is Statement.Block -> IRStmt.IRLabel("ignoreThis")
            is Statement.If -> TODO()
            is MultiAssign -> TODO()
            is Statement.Procedure -> TODO()
            is Statement.Return -> TODO()
            is VarDecl.InitArr -> TODO()
            is VarDecl.RawVarDecl -> TODO()
            is Statement.While -> TODO()
            is Type.Array -> TODO()
            is Primitive.BOOL -> TODO()
            is Primitive.INT -> TODO()
            is Use -> TODO()
            else -> TODO()
        }
    }

    private fun translateExpr(n: Expr): Any {
        return when (n) {
            is Expr.ArrayAccess -> TODO()
            is BinaryOp -> TODO()
            is Expr.FunctionCall -> TODO()
            is Expr.Identifier -> IRTemp("your mom")
            is Expr.FunctionCall.LengthFn -> TODO()
            is Literal.ArrayLit -> TODO()
            is Literal.BoolLit -> IRExpr.IRConst(if (n.bool) 1 else 0)
            is Literal.CharLit -> IRExpr.IRConst(n.char.toLong())
            is Literal.IntLit -> IRExpr.IRConst(n.num)
            is Literal.StringLit -> TODO()
            is UnaryOp -> when (n.op) {
                UnaryOp.Operation.NOT -> TODO()
                UnaryOp.Operation.NEG -> TODO()
            }
        }
    }

//    fun optimize(ir: IRCompUnit): IRCompUnit? {
//        return null;
//    }
//
//    fun lower(ir: IRCompUnit): Unit {
//
//    }

}