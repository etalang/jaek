package typechecker

import ast.*
import ast.BinaryOp.Operation.*
import ast.Expr.FunctionCall.LengthFn
import ast.Literal.*
import ast.UnaryOp.Operation.NEG
import ast.UnaryOp.Operation.NOT
import typechecker.EtaType.ContextType.FunType
import typechecker.EtaType.ContextType.VarBind
import typechecker.EtaType.OrdinaryType.*
import typechecker.EtaType.StatementType.UnitType


class TypeChecker {
    var Gamma : Context = Context()

    fun typeCheck(n : Node) {
        when (n) {
            is Program -> { // two passes here

            }
            is Statement -> { Gamma = typeCheckStmt(n, Gamma) }
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
            is Expr -> typeCheckExpr(n)
            is Type.Array -> TODO()
            is Primitive.BOOL -> TODO()
            is Primitive.INT -> TODO()
            is Use -> TODO()
        }
    }

    fun typeCheckStmt(n:Statement, C:Context): Context {
        when (n) {
            is Statement.ArrayInit -> TODO()
            is Statement.Block -> {
                if (n.stmts.isEmpty()) {
                    n.etaType = UnitType()
                    return C
                }
                else {

                }

            }
            is Statement.If -> {
                typeCheck(n.guard)
                val t = n.guard.etaType
                if (t is BoolType) {
                    typeCheckStmt(n.thenBlock, C)
                    if (n.elseBlock == null) { // no else block? (IF)
                        n.etaType = UnitType()
                    }
                    else {
                        typeCheckStmt(n.elseBlock, C)
                        val r1 = n.thenBlock.etaType
                        val r2 = n.elseBlock.etaType
                        if (r1 is EtaType.StatementType && r2 is EtaType.StatementType) {
                            n.etaType = EtaType.lub(r1, r2)
                        }
                        else {} // throw error, statement types are not returning properly
                    }
                    return C
                }
                else {} // guard is not bool
            }
            is MultiAssign -> TODO()
            is Statement.Procedure -> TODO()
            is Statement.Return -> TODO()
            is VarDecl.InitArr -> TODO()
            is VarDecl.RawVarDecl -> TODO()
            is Statement.While -> {
                typeCheck(n.guard)
                val t = n.guard.etaType
                if (t is BoolType) {
                    typeCheckStmt(n.body, C)
                    n.etaType = UnitType()
                    return C
                }
                else { // guard is not bool

                }
            }
        }
        return C
    }

    fun typeCheckExpr(n:Expr) {
        when (n) {
            is Expr.ArrayAccess -> {
                typeCheck(n.arr)
                typeCheck(n.idx)
                val arrt = n.arr.etaType
                val idxt = n.idx.etaType
                if (arrt is ArrayType) {
                    val t = arrt.t
                    if (idxt is IntType){
                        n.etaType = t
                    }
                    else {} // idx is not valid index
                }
                else {} // arr is not indexable
            }
            is BinaryOp -> {
                typeCheck(n.left)
                typeCheck(n.right)
                val ltype = n.left.etaType
                val rtype = n.right.etaType
                when (ltype) {
                    is IntType -> {
                        if (rtype is IntType) {
                            if (n.op in listOf(PLUS, MINUS, TIMES, HIGHTIMES, DIVIDE, MODULO)) {
                                n.etaType = IntType()
                            } else if (n.op in listOf(EQB, NEQB, LT, LEQ, GT, GEQ)) {
                                n.etaType = BoolType()
                            } else {
                            } // throw error, unsupported operator
                        } else {
                        } // throw error, expecting int, got smth else
                    }
                    is BoolType -> {
                        if (rtype is BoolType) {
                            if (n.op in listOf(EQB, NEQB, AND, OR))
                                n.etaType = BoolType()
                            else {
                            } // throw error, unsupported operator
                        } else {
                        } // throw error, expecting bool, got smth else
                    }
                    is ArrayType -> {
                        val leftBase = ltype.t
                        if (rtype is ArrayType) {
                            val rightBase = rtype.t
                            if (leftBase == rightBase) {
                                if (n.op in listOf(EQB, NEQB))
                                    n.etaType = BoolType()
                                else if (n.op == PLUS) {
                                    n.etaType = ArrayType(ltype)
                                } else {
                                } // throw error, unsupported operator
                            } else {
                            } // throw error, array type mismatch
                        } else {
                        } // throw error, expecting t[], got smth else
                    }

                    else -> {} // throw error, type of left unexpected
                }
            }

            is Expr.FunctionCall -> {
                val ft = Gamma.lookup(n.fn)
                if (ft is FunType) {
                    if (n.args.size == ft.domain.lst.size) {
                        if (ft.codomain.lst.size == 1) {
                            for (i in 0 until n.args.size) {
                                typeCheck(n.args[i])
                                val argtype = n.args[i].etaType
                                if (ft.domain.lst[i] != argtype) {
                                    //release rage on the world for the function inputs are wrong
                                }
                            }
                        } else {
                            //hey! this is not an expression! keep your multioutputs to assigns
                        }
                    } else {
                        //number of arguments mismatch
                    }
                    n.etaType = ft.codomain
                } else {
                    // function does not exist
                }
            }

            is Expr.Identifier -> {
                val t = Gamma.lookup(n.name)
                if (t is VarBind) {
                    n.etaType = t.item
                } else {
                    // throw a SemanticError
                }
            }

            is LengthFn -> {
                typeCheck(n.arg)
                val t = n.arg.etaType
                if (t is ArrayType) {
                    n.etaType = IntType()
                } else {
                } // throw semantic error, expecting array got smth else
            }

            is Literal -> {
                when (n) {
                    is ArrayLit -> { // TODO: HOW TO RESOLVE EMPTY ARRAYS?
                        val typeList = ArrayList<EtaType?>()
                        for (e in n.list) {
                            typeCheck(e)
                            val et = e.etaType
                            typeList.add(et)
                        }
                        n.etaType = ArrayType(IntType())
                        // definitely incorrect but at least it will have something
                    }
                    is BoolLit -> n.etaType = BoolType()
                    is CharLit -> n.etaType = IntType()
                    is IntLit -> n.etaType = IntType()
                    is StringLit -> n.etaType = ArrayType(IntType())
                }
            }

            is UnaryOp -> {
                when (n.op) {
                    NOT -> {
                        typeCheck(n.arg)
                        val t = n.arg.etaType
                        if (t is BoolType) {
                            n.etaType = BoolType()
                        } else {
                            // throw semantic error
                        }
                    }

                    NEG -> {
                        typeCheck(n.arg)
                        val t = n.arg.etaType
                        if (t is IntType) {
                            n.etaType = IntType()
                        } else {
                            // throw semantic error
                        }
                    }
                }
            }
        }
    }
}