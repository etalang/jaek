package typechecker

import ASTUtil
import ast.*
import ast.BinaryOp.Operation.*
import ast.Expr.FunctionCall.LengthFn
import ast.Literal.*
import ast.UnaryOp.Operation.NEG
import ast.UnaryOp.Operation.NOT
import typechecker.EtaType.Companion.translateType
import typechecker.EtaType.ContextType.*
import typechecker.EtaType.ExpandedType
import typechecker.EtaType.OrdinaryType
import typechecker.EtaType.OrdinaryType.*
import typechecker.EtaType.StatementType.UnitType
import typechecker.EtaType.StatementType.VoidType
import java.io.File


class TypeChecker {
    var Gamma : Context = Context()

    fun typeCheck(n : Node) {
        when (n) {
            is Program -> {
                // add interface bindings to the program first
                for (u in n.imports) {
                    typeCheck(u)
                }
                // first pass
                for (defn in n.definitions) {
                    when (defn) {
                        is GlobalDecl -> {
                            if (Gamma.contains(defn.id)) {
                                // throw an error
                            }
                            else {
                                Gamma.bind(defn.id, VarBind(translateType(defn.type)))
                            }
                        }
                        is Method -> {
                            if (Gamma.contains(defn.id)) {
                                val currFunType = Gamma.lookup(defn.id)
                                if (currFunType !is FunType) {
                                    // blow up, should not be bound already to a non-function type
                                }
                                else {
                                    if (!(currFunType.fromInterface)) {
                                        // throw error, cannot redeclare function within program
                                    } else {
                                        var domainList = ArrayList<OrdinaryType>()
                                        for (decl in defn.args) {
                                            domainList.add(translateType(decl.type))
                                        }
                                        var codomainList = ArrayList<OrdinaryType>()
                                        for (t in defn.returnTypes) {
                                            codomainList.add(translateType(t))
                                        }
                                        if (currFunType.domain.lst == domainList &&
                                            currFunType.codomain.lst == codomainList
                                        ) {
                                            currFunType.fromInterface = false
                                            Gamma.bind(defn.id, currFunType)
                                        } else {
                                            // throw an error
                                        }
                                    }
                                }
                            }
                            else {
                                val inputTypes = ArrayList<OrdinaryType>()
                                for (t in defn.args) {
                                    inputTypes.add(translateType(t.type))
                                }
                                val outputTypes = ArrayList<OrdinaryType>()
                                for (t in defn.returnTypes) {
                                    outputTypes.add(translateType(t))
                                }
                                Gamma.bind(defn.id, FunType(ExpandedType(inputTypes), ExpandedType(outputTypes), false))
                            }
                        }
                    }
                }

                // second pass
                for (defn in n.definitions) {
                    when (defn) {
                        is GlobalDecl -> {
                            val vartype = Gamma.lookup(defn.id)
                            if (vartype == null) {
                                // blow up, should be unreachable
                            }
                            else {
                                if (defn.value != null) {
                                    typeCheck(defn.value)
                                    val t = defn.value.etaType
                                    if (t != vartype) {
                                        // blowup, type mismatch
                                    }
                                    // already enforced that it has to be a literal
                                    // OK
                                }
                            }
                        }
                        is Method -> {
                            // check if names are not bound in global scope
                            val argNames = ArrayList<String>()
                            // TODO: fill in gaps here
                            // add bindings

                            // INVARIANT: "@" is the name of the return context varaiable
                            val convertedReturns = ArrayList<OrdinaryType>()
                            for (t in defn.returnTypes) {
                                convertedReturns.add(translateType(t))
                            }
                            Gamma.bind("@", ReturnType(ExpandedType(convertedReturns)))
                        }
                    }
                }
            }
            is Statement -> { typeCheckStmt(n) }
            is Interface -> {
                for (method in n.methodHeaders) {
                    var domainList = ArrayList<OrdinaryType>()
                    for (decl in method.args) {
                        domainList.add(translateType(decl.type))
                    }
                    var codomainList = ArrayList<OrdinaryType>()
                    for (t in method.returnTypes){
                        codomainList.add(translateType(t))
                    }
                    val currFunType = FunType(ExpandedType(domainList), ExpandedType(codomainList), true)
                    if (Gamma.contains(method.id) ) {
                        if (Gamma.lookup(method.id) != currFunType) {
                            // error: mismatch in type of function <method.id> among interfaces/programs
                        }
                    }
                    else {
                        Gamma.bind(method.id, currFunType)
                    }
                }

            }
            is Expr -> typeCheckExpr(n)
            is Use -> {
                val filepath = File(System.getProperty("user.dir"), n.lib + ".eti") // needs to use library path
                val interfaceAST = ASTUtil.getAST(filepath)
                if (interfaceAST is Interface)
                    typeCheck(interfaceAST)
                else {
                    // the interface is not an interface file
                }
            }
            else -> {
                // AssignTarget case -> do nothing, should never be typechecked
                // Type nodes -> should never actually be checked, only referenced and read
                // Definition -> handled at the top level in Program with the multiple passes
            }
        }
    }

    fun typeCheckStmt(n:Statement) {
        when (n) {
            is Statement.Block -> {
                if (n.stmts.isEmpty()) {
                    n.etaType = UnitType()
                }
                else {
                    Gamma.enterScope()
                    for (i in 0 until n.stmts.size) {
                        typeCheck(n.stmts[i])
                        val t = n.stmts[i].etaType
                        if (i < n.stmts.size - 1 && t !is UnitType) {
                            // throw an error, non-unit return type detected
                        }
                    }
                    n.etaType = n.stmts[n.stmts.size - 1].etaType
                    Gamma.leaveScope()
                }
            }
            is Statement.If -> {
                typeCheck(n.guard)
                val t = n.guard.etaType
                if (t is BoolType) {
                    typeCheckStmt(n.thenBlock)
                    if (n.elseBlock == null) { // no else block? (IF)
                        n.etaType = UnitType()
                    }
                    else {
                        typeCheckStmt(n.elseBlock)
                        val r1 = n.thenBlock.etaType
                        val r2 = n.elseBlock.etaType
                        if (r1 is EtaType.StatementType && r2 is EtaType.StatementType) {
                            n.etaType = EtaType.lub(r1, r2)
                        }
                        else {} // throw error, statement types are not returning properly
                    }
                }
                else {} // guard is not bool
            }
            is MultiAssign -> {
                if (n.targets.size != n.vals.size) {
                    // error: # of assignment targets does not match # of assignments
                }
                else {
                    if (n.targets.size == 1) { // single assignment rules
                        val target = n.targets.first()
                        when (target) {
                            is AssignTarget.DeclAssign -> { // VarInit rule
                                if (Gamma.contains(target.decl.id)) {
                                    // error: wtf this is shadowing
                                }
                                else {
                                    typeCheck(n.vals.first())
                                    val t = n.vals.first().etaType
                                    if (t == null || t !is OrdinaryType) {
                                        // error: should be unreachable, invalid var type
                                    }
                                    else {
                                        if (t != translateType(target.decl.type)) {
                                            // error: type mismatch for assignment
                                        }
                                        Gamma.bind(target.decl.id, VarBind(t))
                                        n.etaType = UnitType()
                                    }
                                }
                            }
                            is AssignTarget.ExprAssign -> {
                                typeCheck(n.vals.first())
                                val t = n.vals.first().etaType
                                when (target.target) {
                                    is Expr.ArrayAccess -> { // ArrAssign rule
                                        typeCheck(target.target.arr)
                                        typeCheck(target.target.idx)
                                        val expectedType = target.target.arr.etaType
                                        if (expectedType !is ArrayType) {
                                            // throw error, only arrays indexable
                                        }
                                        else if (target.target.idx.etaType !is IntType){
                                            // throw error, indices must be integers
                                        }
                                        else {
                                            val expected = expectedType.t
                                            if (t != expected) {
                                                // error: unexpected type of expression
                                            }
                                            n.etaType = UnitType()

                                        }
                                    }
                                    is Expr.Identifier -> { // Assign rule
                                        val varType = Gamma.lookup(target.target.name)
                                        if (varType == null || varType !is VarBind) {
                                            // error: wtf the name you want is not a variable
                                        }
                                        else {
                                            val expected = varType.item
                                            if (t != expected) {
                                                // error: mismatch expression type to var type
                                            }
                                            n.etaType = UnitType()
                                        }
                                    }
                                    else -> {
                                        // error: wtf this is not assignable
                                    }
                                }
                            }
                            is AssignTarget.Underscore -> {
                                // error - underscore not permitted in single assignment
                            }
                        }
                    }
                    else { // multiple case

                    }
                }
            }
            is Statement.Procedure -> {
                val fnType = Gamma.lookup(n.id)
                if (fnType == null) {
                    // error: function not bound in scope
                }
                else {
                    if (fnType !is FunType) {
                        // error: id is not bound as function
                    }
                    else {
                        if (fnType.codomain.lst.size != 0) {
                            // error: call is not a procedure
                        }
                        else {
                            if (fnType.domain.lst.size != n.args.size) {
                                // error: # of arguments mismatch
                            }
                            else {
                                for (i in 0 until fnType.domain.lst.size) {
                                    typeCheck(n.args[i])
                                    if (n.args[i].etaType != fnType.domain.lst[i]) {
                                        // error: argument i does not match expected type
                                    }
                                }
                                n.etaType = UnitType()
                            }
                        }
                    }
                }
            }
            is Statement.Return -> {
                // INVARIANT: IF IT EXISTS, THE KEY "@" WILL BE BOUND TO A RETURN TYPE
                val retType = Gamma.lookup("@")
                if (retType == null) {
                    // throw error, return type did not make it into the context??
                }
                else if (retType !is ReturnType) {
                    // throw error, some weird shit happened in binding and this should be unreachable
                }
                else {
                    if (retType.value.lst.size != n.args.size){
                        // error: length of return argument mismatch
                    }
                    for (i in 0 until n.args.size) {
                        typeCheck(n.args[i])
                        if (retType.value.lst[i] != n.args[i].etaType){
                            // error: type mismatch on returned value i
                        }
                    }
                    n.etaType = VoidType()
                }
            }
            is VarDecl.InitArr -> { // ArrayDecl
                if (Gamma.contains(n.id)) {
                    // error: shadowing, already have name in scope
                }
                // TODO: more hypothesis checking + well-formedness of expression
                Gamma.bind(n.id, VarBind(ArrayType(UnknownType())))
                n.etaType = UnitType()
            }
            is VarDecl.RawVarDecl -> { // VarDecl
                if (Gamma.contains(n.id)) {
                    // error: shadowing, already have name in scope
                }
                else {
                    Gamma.bind(n.id, VarBind(translateType(n.type)))
                    n.etaType = UnitType()
                }
            }
            is Statement.While -> {
                typeCheck(n.guard)
                val t = n.guard.etaType
                if (t is BoolType) {
                    typeCheckStmt(n.body)
                    n.etaType = UnitType()
                }
                else { // guard is not bool, throw error
                }
            }
            else -> {
                // ArrayInit - never checked, only referenced as a part of Assign/MultiAssign rules
            }
        }
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
                    is ArrayType -> { // precondition: ArrayTypes can only be formed from OrdinaryTypes
                        val leftBase = ltype.t
                        if (rtype is ArrayType) {
                            val rightBase = rtype.t
                            if (leftBase == rightBase || leftBase is UnknownType || rightBase is UnknownType) {
                                if (n.op in listOf(EQB, NEQB))
                                    n.etaType = BoolType()
                                else if (n.op == PLUS) {
                                    if (leftBase is UnknownType && rightBase !is UnknownType) {
                                        n.etaType = ArrayType(rtype)
                                    }
                                    else if (leftBase is UnknownType && rightBase is UnknownType) {
                                        n.etaType = ArrayType(UnknownType())
                                    }
                                    else {
                                        n.etaType = ArrayType(ltype)
                                    }
                                }
                                else {
                                } // throw error, unsupported operator
                            }
                            else {
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
                    n.etaType = ft.codomain.lst[0] // first (and only) type in list
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
                    is ArrayLit -> {
                        val typeList = ArrayList<EtaType?>()
                        for (e in n.list) {
                            typeCheck(e)
                            val et = e.etaType
                            typeList.add(et)
                        }
                        if (typeList.size == 0){
                            n.etaType = ArrayType(UnknownType())
                        }
                        else {
                            val t = typeList[0]
                            if (t is OrdinaryType) {
                                for (i in 1 until typeList.size) {
                                    if (typeList[i] != t) {
                                        // throw a semantic error, not all types the same
                                    }
                                }
                                n.etaType = ArrayType(t)
                            }
                            else {
                                // t is not ordinary??? blow up
                            }
                        }
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