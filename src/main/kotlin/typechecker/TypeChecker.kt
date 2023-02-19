package typechecker

import ast.*
import ast.BinaryOp.Operation.*
import typechecker.EtaType.OrdinaryType.*
import typechecker.EtaType.VarBind


class TypeChecker {
    var Gamma : Context = Context()

    fun typeCheck(n : Node) {
        when (n) {
            is Program -> { // two passes here

            }
            is Statement -> {
                when (n) {
                    is Statement.ArrayInit -> TODO()
                    is Statement.Block -> TODO()
                    is Statement.If -> TODO()
                    is MultiAssign -> TODO()
                    is Statement.Procedure -> TODO()
                    is Statement.Return -> TODO()
                    is VarDecl.InitArr -> TODO()
                    is VarDecl.RawVarDecl -> TODO()
                    is Statement.While -> TODO()
                }
            }

            is AssignTarget -> {
                when (n) {
                    is AssignTarget.DeclAssign -> TODO()
                    is AssignTarget.ExprAssign -> TODO()
                    is AssignTarget.Underscore -> TODO()
                }
            }
            is Definition -> {
                when (n) {
                    is GlobalDecl -> TODO()
                    is Method -> TODO()
                }
            }
            is Interface -> TODO()
            is Expr -> {
                when (n) {
                    is Expr.ArrayAccess -> TODO()
                    is BinaryOp -> {
                        typeCheck(n.left)
                        typeCheck(n.right)
                        val ltype = n.left.etaType
                        val rtype = n.right.etaType
                        var invalidOp = false
                        if ((ltype is IntType) && (rtype is IntType)) {
                            if (n.op in listOf(PLUS, MINUS, TIMES, HIGHTIMES, DIVIDE, MODULO)){
                                    n.etaType = IntType()
                            } else if (n.op in listOf(EQB, NEQB, LT, LEQ, GT, GEQ)) {
                                    n.etaType = BoolType()
                            } else invalidOp = true
                        } else if ((ltype is BoolType) && (rtype is BoolType)) {
                            if (n.op in listOf(EQB, NEQB, AND, OR))
                                n.etaType = BoolType()
                            else invalidOp = true
                        } else if ((ltype is ArrayType) && (rtype is ArrayType))
                            if (n.op in listOf(EQB, NEQB))
                                n.etaType = BoolType()
                            else invalidOp = true
                        else invalidOp = true
                        //if (invalidOp) throw error
                    }
                    is Expr.FunctionCall -> TODO()
                    is Expr.Identifier -> {
                        val t = Gamma.lookup(n.name)
                        if (t is VarBind) {
                            n.etaType = t.item
                        }
                        else {
                            // throw a SemanticError
                        }
                    }
                    is Expr.FunctionCall.LengthFn -> TODO()
                    is Literal -> {
                        when (n) {
                            is Literal.ArrayLit -> TODO()
                            is Literal.BoolLit -> n.etaType = BoolType()
                            is Literal.CharLit -> n.etaType = IntType()
                            is Literal.IntLit -> n.etaType = IntType()
                            is Literal.StringLit -> n.etaType = ArrayType(IntType())
                        }
                    }

                    is UnaryOp -> TODO()
                }
            }
            is Type.Array -> TODO()
            is Primitive.BOOL -> TODO()
            is Primitive.INT -> TODO()
            is Use -> TODO()
        }
    }
}