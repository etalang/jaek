package typechecker
import ast.Primitive
import ast.Type

sealed class EtaType {
    companion object {
        fun lub(r1 : StatementType, r2: StatementType) : StatementType {
            if (r1 == r2) {
                return r1
            }
            else {
                if (r1 is StatementType.UnitType ||
                    r2 is StatementType.UnitType) {
                    return StatementType.UnitType()
                }
                else {
                    return StatementType.VoidType()
                }
            }
        }

        fun translateType(t:Type) : OrdinaryType {
            when (t) {
                is Type.Array -> return OrdinaryType.ArrayType(translateType(t.t))
                is Primitive.BOOL -> return OrdinaryType.BoolType()
                is Primitive.INT -> return OrdinaryType.IntType()
            }
        }
    }
    sealed class OrdinaryType : EtaType() {
        class IntType : OrdinaryType()
        class BoolType : OrdinaryType()
        class ArrayType(val t : OrdinaryType) : OrdinaryType()
        class UnknownType: OrdinaryType() // for empty arrays, underscores

        override fun equals(other: Any?): Boolean {
            when (other) {
                is IntType -> {
                    if (this is IntType || this is UnknownType) { return true }
                }
                is BoolType -> {
                    if (this is BoolType || this is UnknownType) { return true }
                }
                is ArrayType -> {
                    if (this !is ArrayType) { return false }
                    else return (this.t == other.t)
                }
                is UnknownType -> return true
                else -> return false
            }
            return false
        }
    }

    class ExpandedType(val lst: ArrayList<OrdinaryType>) : EtaType() {
        override fun equals(other: Any?): Boolean {
            if (other !is ExpandedType) return false
            else {
                if (this.lst.size != other.lst.size) {
                    return false
                }
                else {
                    for (i in 0 until this.lst.size) {
                        if (this.lst[i] != other.lst[i]) {
                            return false
                        }
                    }
                    return true
                }
            }
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
    }


    sealed class ContextType : EtaType() {
        class VarBind (val item : OrdinaryType) : ContextType()
        class ReturnType(val value : ExpandedType) : ContextType()
        class FunType (val domain : ExpandedType, val codomain : ExpandedType, var fromInterface : Boolean) : ContextType()

        override fun equals(other: Any?): Boolean {
            if (other !is ContextType) return false
            else {
                when (other) {
                    is VarBind -> return (this is VarBind && this.item == other.item)
                    is ReturnType -> return (this is ReturnType && this.value == other.value)
                    is FunType -> return (this is FunType && this.domain == other.domain && this.codomain == other.codomain)
                }
            }
        }
    }


}