package typechecker

import ast.Primitive
import ast.Statement
import ast.Type
import errors.SemanticError

sealed class EtaType {
    companion object {
        fun lub(r1: StatementType, r2: StatementType): StatementType {
            if (r1 == r2) {
                return r1
            } else {
                if (r1 is StatementType.UnitType ||
                    r2 is StatementType.UnitType
                ) {
                    return StatementType.UnitType()
                } else {
                    return StatementType.VoidType()
                }
            }
        }

        fun translateType(t: Type): OrdinaryType {
            when (t) {
                is Type.Array -> return OrdinaryType.ArrayType(translateType(t.t))
                is Primitive.BOOL -> return OrdinaryType.BoolType()
                is Primitive.INT -> return OrdinaryType.IntType()
                is Type.RecordType -> return OrdinaryType.RecordType(t.t)
            }
        }
    }

    sealed class OrdinaryType : EtaType() {
        class IntType : OrdinaryType()
        class BoolType : OrdinaryType()
        class ArrayType(val t: OrdinaryType) : OrdinaryType()
        class UnknownType(val possiblyBool: Boolean) : OrdinaryType() // for empty arrays, underscores
        class NullType : OrdinaryType()
        class RecordType(val t: String) : OrdinaryType()

        override fun equals(other: Any?): Boolean {
            //TODO: how to deal with unknown
            when (other) {
                is IntType -> {
                    if (this is IntType || this is UnknownType) {
                        return true
                    }
                }
                is BoolType -> {
                    if (this is BoolType || (this is UnknownType && this.possiblyBool)) {
                        return true
                    }
                }
                is ArrayType -> {
                    return when (this) {
                        is UnknownType, is NullType -> { true }
                        !is ArrayType -> { false }
                        else -> (this.t == other.t)
                    }
                }
                is RecordType -> {
                    if (this is RecordType) {
                        return (this.t == other.t)
                    }
                    else if (this is NullType) {
                        return true
                    }
                }
                is NullType -> {
                    if (this is NullType || this is RecordType || this is ArrayType) {
                        return true
                    }
                }
                is UnknownType -> return true
                else -> return false
            }
            return false
        }

        override fun toString(): String {
            return when (this) {
                is ArrayType -> this.t.toString() + "array"
                is BoolType -> "bool"
                is IntType -> "int"
                is UnknownType -> "unk"
                is NullType -> "null"
                is RecordType -> this.t
            }
        }
    }

    class ExpandedType(val lst: ArrayList<OrdinaryType>) : EtaType() {
        override fun equals(other: Any?): Boolean {
            if (other !is ExpandedType) return false
            else {
                if (this.lst.size != other.lst.size) {
                    return false
                } else {
                    for (i in 0 until this.lst.size) {
                        if (this.lst[i] != other.lst[i]) {
                            return false
                        }
                    }
                    return true
                }
            }
        }

        override fun toString(): String {
            return lst.toString()
        }
    }

    sealed class StatementType : EtaType() {
        class UnitType : StatementType()
        class VoidType : StatementType()

        override fun equals(other: Any?): Boolean {
            if (other !is StatementType) return false
            else {
                when (other) {
                    is UnitType -> return (this is UnitType)
                    is VoidType -> return (this is VoidType)
                }
            }
        }

        override fun toString(): String {
            return when (this) {
                is UnitType -> "unit"
                is VoidType -> "void"
            }
        }
    }


    sealed class ContextType : EtaType() {
        class VarBind(val item: OrdinaryType) : ContextType()
        class ReturnType(val value: ExpandedType) : ContextType()
        class FunType(val domain: ExpandedType, val codomain: ExpandedType, var fromInterface: Boolean) :
            ContextType() {
            val argCount get() = domain.lst.size
            val retCount get() = codomain.lst.size
        }

        /* MutableMapOf preserves the iteration order in which elements were added to the map! */
        class RecordType(val name : String, val fields : LinkedHashMap<String, OrdinaryType>) : ContextType() {
            val fieldOrder get() = fields.keys.toList()
            val typeOrder: MutableList<OrdinaryType>
                get() = run {
                val typeList = mutableListOf<OrdinaryType>()
                fields.keys.mapTo(typeList) { it -> fields[it]!! }
                    typeList
            }
        }

        override fun equals(other: Any?): Boolean {
            if (other !is ContextType) return false
            else {
                when (other) {
                    is VarBind -> return (this is VarBind && this.item == other.item)
                    is ReturnType -> return (this is ReturnType && this.value == other.value)
                    is FunType -> return (this is FunType && this.domain == other.domain && this.codomain == other.codomain)
                    is RecordType -> {
                        if (this !is RecordType) return false
                        if (this.name != other.name) return false
                        else {
                            val fieldOrder = this.fieldOrder
                            val typeOrder = this.typeOrder
                            for (idx in 0 until other.fieldOrder.size) {
                                if (fieldOrder[idx] != other.fieldOrder[idx]) {
                                    return false
                                }
                                if (typeOrder[idx] != other.typeOrder[idx]) {
                                    return false
                                }
                            }
                            return true
                        }
                    }
                }
            }
        }

        override fun toString(): String {
            return when (this) {
                is FunType -> "function"
                is ReturnType -> "return" + this.value.toString()
                is VarBind -> this.item.toString() + "variable"
                is RecordType -> "record ${this.name}"
            }
        }
    }


}

typealias EtaFunc = EtaType.ContextType.FunType