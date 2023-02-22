package typechecker

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
    }
    sealed class OrdinaryType : EtaType() {
        class IntType : OrdinaryType()
        class BoolType : OrdinaryType()
        class ArrayType(val t : OrdinaryType) : OrdinaryType()
    }

    class ExpandedType(val lst : ArrayList<OrdinaryType>) : EtaType()

    sealed class StatementType : EtaType() {
        class UnitType : StatementType()
        class VoidType : StatementType()
    }



    sealed class ContextType : EtaType() {
        class VarBind (val item : OrdinaryType) : ContextType()
        class Return(val value : ExpandedType) : ContextType()
        class FunType (val domain : ExpandedType, val codomain : ExpandedType) : ContextType()
    }

}