package typechecker

import ASTUtil
import SemanticError
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
                                throw SemanticError(0,0,"Invalid global variable shadowing")
                            }
                            else {
                                Gamma.bind(defn.id, VarBind(translateType(defn.type)))
                            }
                        }
                        is Method -> {
                            if (Gamma.contains(defn.id)) {
                                val currFunType = Gamma.lookup(defn.id)
                                if (currFunType !is FunType) {
                                    throw SemanticError(0,0,"Invalid function shadowing an existing variable")
                                }
                                else {
                                    if (!(currFunType.fromInterface)) {
                                        throw SemanticError(0,0, "Invalid function shadowing")
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
                                            throw SemanticError(0,0,"Redeclared interface function has invalid different type")
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
                                throw SemanticError(0,0,"Variable not found in second parse pass")
                            }
                            else {
                                if (defn.value != null) {
                                    typeCheck(defn.value)
                                    val t = defn.value.etaType
                                    if (vartype !is VarBind) {
                                        throw SemanticError(0,0,"Global declaration is not for variable")
                                    }
                                    else {
                                        if (t != vartype.item) {
                                            throw SemanticError(0, 0, "global declaration expression type mismatch")
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
                                    throw SemanticError(0, 0, "function parameter $argName shadows variable in global scope")
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
                                throw SemanticError(0, 0, "function in program file missing body")
                            }
                            else {
                                typeCheck(s)
                                if (defn.returnTypes.size != 0) {
                                    if (s.etaType !is VoidType) {
                                        throw SemanticError(0, 0, "function body does not return")
                                    }
                                }
                                // OK, PASS THROUGH
                            }
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
                            throw SemanticError(0, 0, "Mismatch in type of function ${method.id} among interfaces/programs")
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
                try {
                    val interfaceAST = ASTUtil.getAST(filepath)
                    if (interfaceAST is Interface)
                        typeCheck(interfaceAST)
                    else {
                        throw SemanticError(0, 0, "Could not import interface ${n.lib} AST")
                    }
                } catch (e : FileNotFoundException) {
                    throw SemanticError(0, 0, "Could not find interface ${n.lib} file")

                }
            }
            else -> {
                throw SemanticError(0,0,"Unreachable, calling typeCheck on Node that should not be type-checked explicitly")
                // AssignTarget case -> do nothing, should never be typechecked from here
                // Type nodes -> should never actually be checked, only referenced and read
                // Definition -> handled at the top level in Program with the multiple passes
            }
        }
    }

    /** typeCheckAssignHelp(n, et, gammai) executes the judgement
     * Gamma, (Gamma :: gammai) |- n :: t -| (Gamma : gammai'), where gammai' is the
     * returned map from typeCheckAssignHelp */
    fun typeCheckAssignHelp (n:AssignTarget, expectedType:EtaType?, gammai : HashMap<String, ContextType>) : HashMap<String, ContextType> {
        if (expectedType == null) {
            throw SemanticError(0, 0, "unreachable, should have just created this type")
        }
        else {
            when (n) {
                is AssignTarget.ArrayAssign -> {
                    typeCheck(n.arrayAssign.arr)
                    typeCheck(n.arrayAssign.idx)
                    if (n.arrayAssign.arr.etaType !is ArrayType) {
                        throw SemanticError(0, 0, "Indexed expression is not an array")
                    }
                    else {
                        if (n.arrayAssign.idx.etaType !is IntType) {
                            throw SemanticError(0, 0, "Indexing expression is not an integer")
                        }
                        else {
                            n.etaType = (n.arrayAssign.arr.etaType as ArrayType).t
                        }
                    }
                }
                is AssignTarget.DeclAssign -> {
                    if (Gamma.contains(n.decl.id) || gammai.containsKey(n.decl.id)) {
                        throw SemanticError(0, 0, "Shadowing old variable ${n.decl.id} in multiassignment")
                    }
                    val t = translateType(n.decl.type)
                    n.etaType = t
                    gammai[n.decl.id] = VarBind(t)
                }
                is AssignTarget.IdAssign -> {
                    val t = Gamma.lookup(n.idAssign.name)
                    if (t !is VarBind) {
                        throw SemanticError(0,0,"Assignment target not a variable")
                    }
                    else {
                        n.etaType = t.item
                    }
                }
                is AssignTarget.Underscore -> { n.etaType = UnknownType() }
            }
            return gammai
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
                            throw SemanticError(0, 0, "Function block should have void type")
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
                            throw SemanticError(0,0,"One of these if/else blocks is not a StatementType")
                        }
                    }
                }
                else {
                    throw SemanticError(0,0,"If statement guard must be type boolean")
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
                                    throw SemanticError(0,0,"number of arguments mismatch in function call")
                                }
                                else {
                                    for (i in 0 until f.args.size) {
                                        typeCheck(f.args[i])
                                        if (f.args[i].etaType != fType.domain.lst[i]) {
                                            throw SemanticError(0, 0, "type mismatch as argument to function")
                                        }
                                    }
                                    if (n.targets.size != fType.codomain.lst.size) {
                                        throw SemanticError(0,0,"Number of assignment targets mismatch from function")
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
                                throw SemanticError(0,0,"${f.fn} not bound as a function")
                            }
                        }
                        else {
                            throw SemanticError(0,0,"Cannot multi-assign with a non-function call")
                        }
                    }
                    else {
                        throw SemanticError(0, 0, "number of assignment targets does not match number of assignments")
                    }
                }
                else {
                    if (n.targets.size == 1) { // single assignment rules
                        val target = n.targets.first()
                        when (target) {
                            is AssignTarget.DeclAssign -> { // VarInit rule
                                if (Gamma.contains(target.decl.id)) {
                                    throw SemanticError(0,0,"Identifier ${target.decl.id} already exists in scope")
                                }
                                else {
                                    typeCheck(n.vals.first())
                                    val t = n.vals.first().etaType
                                    if (t == null || t !is OrdinaryType) {
                                        throw SemanticError(0,0,"Unreachable, variable type is not valid")
                                    }
                                    else {
                                        if (t != translateType(target.decl.type)) {
                                            throw SemanticError(0,0,"Assigned expression type does not match expected type ${target.decl.type}")
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
                                    throw SemanticError(0,0,"Type of indexed expression is not an array")
                                }
                                else if (target.arrayAssign.idx.etaType !is IntType){
                                    throw SemanticError(0,0,"Type of indexing expression is not an integer")
                                }
                                else {
                                    val expected = expectedType.t
                                    if (t != expected) {
                                        throw SemanticError(0,0,"Cannot assign to array of type ${expected}[]")
                                    }
                                    n.etaType = UnitType()

                                }
                            }
                            is AssignTarget.IdAssign -> {
                                typeCheck(n.vals.first())
                                val t = n.vals.first().etaType
                                val varType = Gamma.lookup(target.idAssign.name)
                                if (varType == null || varType !is VarBind) {
                                    throw SemanticError(0,0,"Cannot assign to non-assignable type $varType")
                                }
                                else {
                                    val expected = varType.item
                                    if (t != expected) {
                                        throw SemanticError(0,0, "Cannot assign type $t to variable of type $expected")
                                    }
                                    n.etaType = UnitType()
                                }
                            }
                            is AssignTarget.Underscore -> {
                                throw SemanticError(0,0,"Underscore not permitted in single assignment")
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
                    throw SemanticError(0,0,"Name ${n.id} of procedure is not bound in scope")
                }
                else {
                    if (fnType !is FunType) {
                        throw SemanticError(0,0,"Name ${n.id} is not bound as a procedure")
                    }
                    else {
                        if (fnType.codomain.lst.size != 0) {
                            throw SemanticError(0,0,"Procedures may not return any values")
                        }
                        else {
                            if (fnType.domain.lst.size != n.args.size) {
                                throw SemanticError(0,0,"Procedure ${n.id} expected ${fnType.domain.lst.size} arguments," +
                                        " received ${n.args.size}")
                            }
                            else {
                                for (i in 0 until fnType.domain.lst.size) {
                                    typeCheck(n.args[i])
                                    if (n.args[i].etaType != fnType.domain.lst[i]) {
                                        throw SemanticError(0,0,"Argument at position $i does not match procedure's expected type")

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
                    throw SemanticError(0,0,"No return type found for function (not even unit!)")
                }
                else if (retType !is ReturnType) {
                    throw SemanticError(0,0,"The return is not of expected return type")
                }
                else {
                    if (retType.value.lst.size != n.args.size){
                        throw SemanticError(0,0,"Expected return of size ${retType.value.lst.size}, but returned" +
                                "only ${n.args.size}")
                    }
                    for (i in 0 until n.args.size) {
                        typeCheck(n.args[i])
                        if (retType.value.lst[i] != n.args[i].etaType){
                            throw SemanticError(0,0,"Return type expected ${n.args[i].etaType}" +
                                    " at position $i, actual return was ${retType.value.lst[i]}")
                        }
                    }
                    n.etaType = VoidType()
                }
            }
            is VarDecl.InitArr -> { // ArrayDecl
                if (Gamma.contains(n.id)) {
                    throw SemanticError(0,0,"Identifier ${n.id} already exists")
                }
                val t = translateType(n.arrInit.type)
                if (t !is IntType && t !is BoolType) {
                    throw SemanticError(0, 0, "Base type of array not well-formed")
                }
                var firstNonEmpty = -1
                for (i in 0 until n.arrInit.dimensions.size) {
                    if (n.arrInit.dimensions[i] != null) {
                        firstNonEmpty = i
                        break
                    }
                }
                if (firstNonEmpty == -1) {
                    throw SemanticError(0, 0, "Array initialization should have at least one declared dimension")
                }
                for (j in firstNonEmpty until n.arrInit.dimensions.size) {
                    if (n.arrInit.dimensions[j] == null) {
                        throw SemanticError(0,0,"Incorrect dimensions for array initialization")
                    }
                    else {
                        n.arrInit.dimensions[j]?.let { typeCheck(it) }
                        if (n.etaType !is IntType){
                            throw SemanticError(0, 0, "Initialization dimension not an integer")
                        }
                    }
                }
                var boundType = t
                for (k in 0 until n.arrInit.dimensions.size) {
                    boundType = ArrayType(t)
                }
                Gamma.bind(n.id, VarBind(boundType))
                n.etaType = UnitType()
            }
            is VarDecl.RawVarDecl -> { // VarDecl
                if (Gamma.contains(n.id)) {
                    throw SemanticError(0,0,"Identifier ${n.id} already exists")
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
                    throw SemanticError(0,0,"While statement guard must be type boolean")
                }
            }
            else -> {
                throw SemanticError(0,0,"Unreachable, calling typeCheckStmt on Statement node that should not be type-checked explicitly")
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
                        throw SemanticError(0,0,"Index must be an integer")
                    }
                }
                else {
                    throw SemanticError(0,0,"Expression is not indexable")
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
                                throw SemanticError(0,0,"Integers cannot be used with ${n.op}")
                            }
                        } else {
                            throw SemanticError(0,0,"Invalid binop ${n.op} attempted with integer and non-integer")
                        }
                    }
                    is BoolType -> {
                        if (rtype is BoolType) {
                            if (n.op in listOf(EQB, NEQB, AND, OR))
                                n.etaType = BoolType()
                            else {
                                throw SemanticError(0,0,"Booleans cannot be used with ${n.op}")
                            }
                        } else {
                            throw SemanticError(0,0,"Invalid binop ${n.op} attempted with boolean and non-boolean")
                        }
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
                                    throw SemanticError(0,0,"Arrays cannot be used with ${n.op}")
                                }
                            }
                            else {
                                throw SemanticError(0,0,"Binop ${n.op} attempted with arrays with mismatched types")
                            }
                        } else {
                            throw SemanticError(0,0,"Binop ${n.op} attempted with array and non-array")
                        }
                    }

                    else -> {
                        throw SemanticError(0,0,"Operation ${n.op} attempted with impossible type")
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
                                    throw SemanticError(0,0,"Function ${n.fn} expected ${ft.domain.lst[i]} as input" +
                                            " at position $i, received $argtype")
                                }
                            }
                        } else {
                            throw SemanticError(0,0,"Function ${n.fn} tried to output multiple returns as an expression")
                        }
                    } else {
                        throw SemanticError(0,0,"Function ${n.fn} expected ${ft.domain.lst.size} arguments," +
                                " received ${n.args.size}")
                    }
                    n.etaType = ft.codomain.lst[0] // first (and only) type in list
                } else {
                    throw SemanticError(0,0,"${n.fn} is not a defined function")
                }
            }

            is Expr.Identifier -> {
                val t = Gamma.lookup(n.name)
                if (t is VarBind) {
                    n.etaType = t.item
                } else {
                    throw SemanticError(0,0,"Identifier ${n.name} not bound to a variable")
                }
            }

            is LengthFn -> {
                typeCheck(n.arg)
                val t = n.arg.etaType
                if (t is ArrayType) {
                    n.etaType = IntType()
                } else {
                    throw SemanticError(0,0,"Length function must be applied to an array")
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
                                        throw SemanticError(0,0,"Array elements must be consistent type")
                                    }
                                }
                                n.etaType = ArrayType(t)
                            }
                            else {
                                throw SemanticError(0,0,"Array elements music be int, bool, or array")
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
                            throw SemanticError(0,0,"Not must be applied to boolean")
                        }
                    }

                    NEG -> {
                        typeCheck(n.arg)
                        val t = n.arg.etaType
                        if (t is IntType) {
                            n.etaType = IntType()
                        } else {
                            throw SemanticError(0,0,"Negative must be applied to integer")
                        }
                    }
                }
            }
        }
    }
}