package typechecker

import ASTUtil
import errors.SemanticError
import ast.*
import ast.BinaryOp.Operation.*
import ast.Expr.FunctionCall.LengthFn
import ast.Literal.*
import ast.UnaryOp.Operation.NEG
import ast.UnaryOp.Operation.NOT
import typechecker.EtaType.*
import typechecker.EtaType.Companion.translateType
import typechecker.EtaType.ContextType.*
import typechecker.EtaType.OrdinaryType.*
import typechecker.EtaType.StatementType.UnitType
import typechecker.EtaType.StatementType.VoidType
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception


class TypeChecker(topGamma : Context) {
    var Gamma : Context = topGamma

    @Throws(SemanticError::class)
    private fun semanticError(node : Node, msg: String) {
        throw SemanticError(node.terminal.line,node.terminal.column,msg)
    }

    fun typeCheck(n : Node) {
        when (n) {
            is Program -> {
                // first pass
                for (defn in n.definitions) {
                    when (defn) {
                        is GlobalDecl -> {
                            if (Gamma.contains(defn.id)) {
                                semanticError(defn,"Invalid global variable shadowing")
                            }
                            else {
                                Gamma.bind(defn.id, VarBind(translateType(defn.type)))
                            }
                        }
                        is Method -> {
                            if (Gamma.contains(defn.id)) {
                                val currFunType = Gamma.lookup(defn.id)
                                if (currFunType !is FunType) {
                                    semanticError(defn,"Invalid function shadowing an existing variable")
                                }
                                else {
                                    if (!(currFunType.fromInterface)) {
                                        semanticError(defn, "Invalid function shadowing")
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
                                            semanticError(defn,"Redeclared interface function has invalid different type")
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
                                semanticError(defn,"Variable not found in second parse pass")
                            }
                            else {
                                if (defn.value != null) {
                                    typeCheck(defn.value)
                                    val t = defn.value.etaType
                                    if (vartype !is VarBind) {
                                        semanticError(defn,"Global declaration is not for variable")
                                    }
                                    else {
                                        if (t != vartype.item) {
                                            semanticError(defn, "global declaration expression type mismatch")
                                        }
                                    }
                                    // already enforced that it has to be a literal
                                    // OK, PASS THROUGH
                                }
                            }
                        }
                        is Method -> {
                            // check if names are not bound in global scope
                            Gamma.enterScope()
                            for (decl in defn.args) {
                                val argName = decl.id
                                if (Gamma.contains(argName)) {
                                    semanticError(decl, "function parameter $argName shadows variable in global scope")
                                }
                                Gamma.bind(argName, VarBind(translateType(decl.type)))
                            }

                            // INVARIANT: "@" is the name of the return context varaiable
                            val convertedReturns = ArrayList<OrdinaryType>()
                            for (t in defn.returnTypes) {
                                convertedReturns.add(translateType(t))
                            }
                            Gamma.bind("@", ReturnType(ExpandedType(convertedReturns)))
                            val s = defn.body
                            if (s == null) {
                                semanticError(defn, "function in program file missing body")
                            }
                            else {
                                typeCheck(s)
                                if (defn.returnTypes.size != 0) {
                                    if (s.etaType !is VoidType) {
                                        semanticError(s, "function body does not return") //TODO
                                    }
                                }
                                // OK, PASS THROUGH
                            }
                            Gamma.leaveScope()
                        }
                    }
                }
            }
            is Statement -> { typeCheckStmt(n) }
            is Expr -> typeCheckExpr(n)
            else -> {
                semanticError(n,"Unreachable, calling typeCheck on Node that should not be type-checked explicitly")
                // AssignTarget case -> do nothing, should never be typechecked from here
                // Type nodes -> should never actually be checked, only referenced and read
                // Definition -> handled at the top level in Program with the multiple passes
                // Interface, Use should be handled by top-level constructor
            }
        }
    }

    /** typeCheckAssignHelp(n, et, gammai) executes the judgement
     * Gamma, (Gamma :: gammai) |- n :: t -| (Gamma : gammai'), where gammai' is the
     * returned map from typeCheckAssignHelp */
    fun typeCheckAssignHelp (n:AssignTarget, expectedType:EtaType?, gammai : HashMap<String, ContextType>) : HashMap<String, ContextType> {
        if (expectedType == null) {
            semanticError(n, "unreachable, should have just created this type")
        }
        else {
            when (n) {
                is AssignTarget.ArrayAssign -> {
                    typeCheck(n.arrayAssign.arr)
                    typeCheck(n.arrayAssign.idx)
                    if (n.arrayAssign.arr.etaType !is ArrayType) {
                        semanticError(n.arrayAssign.arr, "Indexed expression is not an array")
                    }
                    else {
                        if (n.arrayAssign.idx.etaType !is IntType) {
                            semanticError(n.arrayAssign.idx, "Indexing expression is not an integer")
                        }
                        else {
                            val arrType = n.arrayAssign.arr.etaType
                            if (arrType is ArrayType) {
                                val arrT = arrType.t
                                if (arrT != expectedType) {
                                    semanticError(n.arrayAssign, "Type mismatch on array assignment")
                                }
                                n.etaType = arrType.t
                            }
                            else {
                                semanticError(n.arrayAssign, "Array assign is not assigning to an array")
                            }
                        }
                    }
                }
                is AssignTarget.DeclAssign -> {
                    if (Gamma.contains(n.decl.id) || gammai.containsKey(n.decl.id)) {
                        semanticError(n.decl, "Shadowing old variable ${n.decl.id} in multiassignment")
                    }
                    val t = translateType(n.decl.type)
                    if (t != expectedType) {
                        semanticError(n.decl, "Type mismatch on declaration assignment")
                    }
                    n.etaType = t
                    gammai[n.decl.id] = VarBind(t)
                }
                is AssignTarget.IdAssign -> {
                    val t = Gamma.lookup(n.idAssign.name)
                    if (t !is VarBind) {
                        semanticError(n.idAssign,"Assignment target not a variable")
                    }
                    else {
                        val idT = t.item
                        if (idT != expectedType){
                            semanticError(n.idAssign, "Type mismatch on identifier assignment")
                        }
                        n.etaType = t.item
                    }
                }
                is AssignTarget.Underscore -> { n.etaType = UnknownType() }
            }
            return gammai
        }
        throw Exception("not sure how this happened")
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
                            semanticError(n.stmts[i], "Function block should have void type")
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
                        if (r1 is StatementType && r2 is StatementType) {
                            n.etaType = EtaType.lub(r1, r2)
                        }
                        else {
                            semanticError(n,"One of these if/else blocks is not a StatementType")
                        }
                    }
                }
                else {
                    semanticError(n.guard,"If statement guard must be type boolean")
                }
            }
            is MultiAssign -> {
                if (n.targets.size != n.vals.size) {
                    // MultiAssignCall
                    if (n.vals.size == 1){
                        val f = n.vals[0]
                        if (f is Expr.FunctionCall) {
                            val fType = Gamma.lookup(f.fn)
                            if (fType is FunType) {
                                if (f.args.size != fType.domain.lst.size) {
                                    semanticError(n,"number of arguments mismatch in function call")
                                }
                                else {
                                    for (i in 0 until f.args.size) {
                                        typeCheck(f.args[i])
                                        if (f.args[i].etaType != fType.domain.lst[i]) {
                                            semanticError(f.args[i], "type mismatch as argument to function")
                                        }
                                    }
                                    if (n.targets.size != fType.codomain.lst.size) {
                                        semanticError(n,"Number of assignment targets mismatch from function")
                                    }
                                    else {
                                        var newBindings = HashMap<String, ContextType>()
                                        for (i in 0 until fType.codomain.lst.size) {
                                            newBindings = typeCheckAssignHelp(n.targets[i], fType.codomain.lst[i], newBindings)
                                        }
                                        for (k in newBindings.keys) {
                                            newBindings[k]?.let { Gamma.bind(k, it) }
                                        }
                                        n.etaType = UnitType()
                                    }
                                }
                            }
                            else {
                                semanticError(f,"${f.fn} not bound as a function")
                            }
                        }
                        else {
                            semanticError(n,"Cannot multi-assign with a non-function call") //TODO
                        }
                    }
                    else {
                        semanticError(n, "number of assignment targets does not match number of assignments")
                    }
                }
                else {
                    if (n.targets.size == 1) { // single assignment rules
                        val target = n.targets.first()
                        when (target) {
                            is AssignTarget.DeclAssign -> { // VarInit rule
                                if (Gamma.contains(target.decl.id)) {
                                    semanticError(target.decl,"Identifier ${target.decl.id} already exists in scope")
                                }
                                else {
                                    typeCheck(n.vals.first())
                                    val t = n.vals.first().etaType
                                    if (t == null || t !is OrdinaryType) {
                                        semanticError(n.vals.first(),"Unreachable, variable type is not valid") //TODO
                                    }
                                    else {
                                        if (t != translateType(target.decl.type)) {
                                            semanticError(target.decl,"Assigned expression type does not match expected type ${target.decl.type}")
                                        }
                                        Gamma.bind(target.decl.id, VarBind(t))
                                        n.etaType = UnitType()
                                    }
                                }
                            }
                            is AssignTarget.ArrayAssign -> { // ArrAssign rule
                                typeCheck(n.vals.first())
                                val t = n.vals.first().etaType
                                typeCheck(target.arrayAssign.arr)
                                typeCheck(target.arrayAssign.idx)
                                val expectedType = target.arrayAssign.arr.etaType
                                if (expectedType !is ArrayType) {
                                    semanticError(target.arrayAssign.idx,"Type of indexed expression is not an array")
                                }
                                else if (target.arrayAssign.idx.etaType !is IntType){
                                    semanticError(target.arrayAssign.idx,"Type of indexing expression is not an integer")
                                }
                                else {
                                    val expected = expectedType.t
                                    if (t != expected) {
                                        semanticError(target.arrayAssign,"Cannot assign to array of type ${expected}[]")
                                    }
                                    n.etaType = UnitType()

                                }
                            }
                            is AssignTarget.IdAssign -> {
                                typeCheck(n.vals.first())
                                val t = n.vals.first().etaType
                                val varType = Gamma.lookup(target.idAssign.name)
                                if (varType == null || varType !is VarBind) {
                                    semanticError(target.idAssign,"Cannot assign to non-assignable of type $varType")
                                }
                                else {
                                    val expected = varType.item
                                    if (t != expected) {
                                        semanticError(target, "Cannot assign type $t to variable of type $expected")
                                    }
                                    n.etaType = UnitType()
                                }
                            }
                            is AssignTarget.Underscore -> {
                                typeCheck(n.vals.first())
                                n.etaType = UnitType()
//                                throw SemanticError(0,0,"Underscore not permitted in single assignment")
                            }
                        }
                    }
                    else { // multiple case
                        // generate the t_is
                        for (i in 0 until n.targets.size) {
                            typeCheck(n.vals[i])
                        }
                        var newBindings = HashMap<String, ContextType>()
                        for (i in 0 until n.targets.size) {
                            newBindings = typeCheckAssignHelp(n.targets[i], n.vals[i].etaType, newBindings)
                        }
                        for (k in newBindings.keys) {
                            newBindings[k]?.let { Gamma.bind(k, it) }
                        }
                        n.etaType = UnitType()
                    }
                }
            }
            is Statement.Procedure -> {
                val fnType = Gamma.lookup(n.id)
                if (fnType == null) {
                    semanticError(n,"Name ${n.id} of procedure is not bound in scope")
                }
                else {
                    if (fnType !is FunType) {
                        semanticError(n,"Name ${n.id} is not bound as a procedure")
                    }
                    else {
                        if (fnType.codomain.lst.size != 0) {
                            semanticError(n,"Procedures may not return any values")
                        }
                        else {
                            if (fnType.domain.lst.size != n.args.size) {
                                semanticError(n,"Procedure ${n.id} expected ${fnType.domain.lst.size} arguments," +
                                        " received ${n.args.size}")
                            }
                            else {
                                for (i in 0 until fnType.domain.lst.size) {
                                    typeCheck(n.args[i])
                                    if (n.args[i].etaType != fnType.domain.lst[i]) {
                                        semanticError(n.args[i],"Argument at position $i does not match procedure's expected type")

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
                    semanticError(n,"No return type found for function (not even unit!)")
                }
                else if (retType !is ReturnType) {
                    semanticError(n,"The return is not of expected return type")
                }
                else {
                    if (retType.value.lst.size != n.args.size){
                        semanticError(n,"Expected return of size ${retType.value.lst.size}, but returned" +
                                "only ${n.args.size}")
                    }
                    for (i in 0 until n.args.size) {
                        typeCheck(n.args[i])
                        if (retType.value.lst[i] != n.args[i].etaType){
                            semanticError(n.args[i],"Return type expected ${n.args[i].etaType}" +
                                    " at position $i, actual return was ${retType.value.lst[i]}")
                        }
                    }
                    n.etaType = VoidType()
                }
            }
            is VarDecl.InitArr -> { // ArrayDecl
                if (Gamma.contains(n.id)) {
                    semanticError(n,"Identifier ${n.id} already exists")
                }
                val t = translateType(n.arrInit.type)
                if (t !is IntType && t !is BoolType) {
                    semanticError(n, "Base type of array not well-formed")
                }
                var firstNonEmpty = -1
                for (i in 0 until n.arrInit.dimensions.size) {
                    if (n.arrInit.dimensions[i] != null) {
                        firstNonEmpty = i
                        break
                    }
                }
                if (firstNonEmpty == -1) {
                    semanticError(n, "Array initialization should have at least one declared dimension")
                }
                for (j in firstNonEmpty until n.arrInit.dimensions.size) {
                    if (n.arrInit.dimensions[j] == null) {
                        semanticError(n,"Incorrect dimensions for array initialization")
                    }
                    else {
                        n.arrInit.dimensions[j]?.let { typeCheck(it) }
                        if (n.arrInit.dimensions[j]?.etaType !is IntType){
                            semanticError(n.arrInit, "Initialization dimension not an integer")
                        }
                    }
                }
                var boundType = t
                for (k in 0 until n.arrInit.dimensions.size) {
                    boundType = ArrayType(boundType)
                }
                Gamma.bind(n.id, VarBind(boundType))
                n.etaType = UnitType()
            }
            is VarDecl.RawVarDecl -> { // VarDecl
                if (Gamma.contains(n.id)) {
                    semanticError(n,"Identifier ${n.id} already exists")
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
                else {
                    semanticError(n.guard,"While statement guard must be type boolean")
                }
            }
            else -> {
                semanticError(n,"Unreachable, calling typeCheckStmt on Statement node that should not be type-checked explicitly")
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
                    else {
                        semanticError(n.idx,"Index must be an integer")
                    }
                }
                else {
                    semanticError(n.arr,"Expression is not indexable")
                }
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
                                semanticError(n,"Integers cannot be used with ${n.op}")
                            }
                        } else {
                            semanticError(n,"Invalid binop ${n.op} attempted with integer and non-integer")
                        }
                    }
                    is BoolType -> {
                        if (rtype is BoolType) {
                            if (n.op in listOf(EQB, NEQB, AND, OR))
                                n.etaType = BoolType()
                            else {
                                semanticError(n,"Booleans cannot be used with ${n.op}")
                            }
                        } else {
                            semanticError(n,"Invalid binop ${n.op} attempted with boolean and non-boolean")
                        }
                    }
                    is ArrayType -> { // precondition: ArrayTypes can only be formed from OrdinaryTypes
                        //TODO: probably want to report on the bad expr?
                        val leftBase = ltype.t
                        if (rtype is ArrayType) {
                            val rightBase = rtype.t
                            if (leftBase == rightBase || leftBase is UnknownType || rightBase is UnknownType) {
                                if (n.op in listOf(EQB, NEQB))
                                    n.etaType = BoolType()
                                else if (n.op == PLUS) {
                                    if (leftBase is UnknownType && rightBase !is UnknownType) {
                                        n.etaType = rtype
                                    }
                                    else if (leftBase is UnknownType) {
                                        n.etaType = ArrayType(UnknownType())
                                    }
                                    else {
                                        n.etaType = ltype
                                    }
                                }
                                else {
                                    semanticError(n,"Arrays cannot be used with ${n.op}")
                                }
                            }
                            else {
                                semanticError(n,"Binop ${n.op} attempted with arrays with mismatched types")
                            }
                        } else {
                            semanticError(n,"Binop ${n.op} attempted with array and non-array")
                        }
                    }
                    is UnknownType -> {
                        when (rtype) {
                            is IntType -> { // duplicated code
                                if (n.op in listOf(PLUS, MINUS, TIMES, HIGHTIMES, DIVIDE, MODULO)) {
                                    n.etaType = IntType()
                                    n.left.etaType = IntType()
                                } else if (n.op in listOf(EQB, NEQB, LT, LEQ, GT, GEQ)) {
                                    n.etaType = BoolType()
                                    n.left.etaType = BoolType()
                                } else {
                                    semanticError(n,"Integers cannot be used with ${n.op}")
                                }
                            }
                            is BoolType -> {
                                if (n.op in listOf(EQB, NEQB, AND, OR)){
                                    n.etaType = BoolType()
                                n.left.etaType = BoolType()
                                }
                                else {
                                    semanticError(n,"Booleans cannot be used with ${n.op}")
                                }
                            }
                            is ArrayType -> {
                                val rightBase = rtype.t
                                if (n.op in listOf(EQB, NEQB)) {
                                    n.etaType = BoolType()
                                    n.left.etaType = ArrayType(rightBase)
                                }
                                else if (n.op == PLUS) {
                                    if (rightBase !is UnknownType) {
                                        n.etaType = rtype
                                        n.left.etaType = ArrayType(rightBase)
                                    }
                                    else {
                                        n.etaType = ArrayType(UnknownType())
                                    }
                                }
                                else {
                                    semanticError(n,"Unknown array cannot be used with ${n.op}")
                                }
                            }
                            is UnknownType -> { n.etaType = UnknownType() }
                            else -> {
                                semanticError(n, "Cannot do expression operation with non-expression type")
                            }
                        }
                    }
                    else -> {
                        semanticError(n,"Operation ${n.op} attempted with impossible type")
                    }
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
                                    semanticError(n,"Function ${n.fn} expected ${ft.domain.lst[i]} as input" +
                                            " at position $i, received $argtype")
                                }
                            }
                        } else {
                            if (ft.codomain.lst.size > 1) {
                                semanticError(n,"Function ${n.fn} tried to output multiple returns as an expression")}
                            else {
                                semanticError(n,"Function ${n.fn} tried to output no returns as an expression")
                            }
                        }
                    } else {
                        semanticError(n,"Function ${n.fn} expected ${ft.domain.lst.size} arguments," +
                                " received ${n.args.size}")
                    }
                    n.etaType = ft.codomain.lst[0] // first (and only) type in list
                } else {
                    semanticError(n,"${n.fn} is not a defined function")
                }
            }

            is Expr.Identifier -> {
                val t = Gamma.lookup(n.name)
                if (t is VarBind) {
                    n.etaType = t.item
                } else {
                    semanticError(n,"Identifier ${n.name} not bound to a variable")
                }
            }

            is LengthFn -> {
                typeCheck(n.arg)
                val t = n.arg.etaType
                if (t is ArrayType) {
                    n.etaType = IntType()
                } else {
                    semanticError(n,"Length function must be applied to an array")
                }
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
                                        semanticError(n,"Array elements must be consistent type")
                                    }
                                }
                                n.etaType = ArrayType(t)
                            }
                            else {
                                semanticError(n,"Array elements music be int, bool, or array")
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
                            semanticError(n,"Not must be applied to boolean")
                        }
                    }

                    NEG -> {
                        typeCheck(n.arg)
                        val t = n.arg.etaType
                        if (t is IntType) {
                            n.etaType = IntType()
                        } else {
                            semanticError(n,"Negative must be applied to integer")
                        }
                    }
                }
            }
        }
    }
}