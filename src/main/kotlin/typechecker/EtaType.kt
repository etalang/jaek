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
    }

    class ExpandedType(val lst: ArrayList<OrdinaryType>) : EtaType()

    sealed class StatementType : EtaType() {
        class UnitType : StatementType()
        class VoidType : StatementType()
    }


    sealed class ContextType : EtaType() {
        class VarBind (val item : OrdinaryType) : ContextType()
        class ReturnType(val value : ExpandedType) : ContextType()
        class FunType (val domain : ExpandedType, val codomain : ExpandedType, var fromInterface : Boolean) : ContextType()
    }

}