package ir

import ast.*
import ir.IRExpr.*
import ir.IRStmt.*

class IRTranslator {

    fun translate(n : Node) {
        when (n) {
            is AssignTarget.ArrayAssign -> TODO()
            is AssignTarget.DeclAssign -> TODO()
            is AssignTarget.IdAssign -> TODO()
            is AssignTarget.Underscore -> TODO()
            is GlobalDecl -> TODO()
            is Method -> TODO()
            is Interface -> TODO()
            is Program -> TODO()
            is Expr -> translateExpr(n)
            is Statement.ArrayInit -> TODO()
            is Statement.Block -> TODO()
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
        }
    }

    fun translateExpr (n : Expr) {
        when (n) {
            is Expr.ArrayAccess -> TODO()
            is BinaryOp -> TODO()
            is Expr.FunctionCall -> TODO()
            is Expr.Identifier -> IRTemp("your mom")
            is Expr.FunctionCall.LengthFn -> TODO()
            is Literal.ArrayLit -> TODO()
            is Literal.BoolLit -> TODO() // IRConst(n.bool ? 1 else 0)
            is Literal.CharLit -> TODO()
            is Literal.IntLit -> TODO()
            is Literal.StringLit -> TODO()
            is UnaryOp -> TODO()
        }
    }
}