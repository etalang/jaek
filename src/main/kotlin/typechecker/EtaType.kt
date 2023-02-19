package typechecker

sealed class EtaType {
    sealed class OrdinaryType : EtaType() {
        class IntType : OrdinaryType()
        class BoolType : OrdinaryType()
        class ArrayType(val t : OrdinaryType) : OrdinaryType()
    }

    sealed class ExpandedType : EtaType() {
        class Ordinary(val o : OrdinaryType) : ExpandedType()
        class Unit : ExpandedType()
        class Tuple(val lst : List<OrdinaryType>) : ExpandedType()

        fun subType(e1 : ExpandedType, e2 : ExpandedType) {
            when (e1) {
                is Ordinary -> when (e2) {
                    is Ordinary -> true
                    is Tuple -> false
                    is Unit -> true
                }
                is Tuple -> e2 is Tuple
                is Unit -> e2 is Unit
            }
        }
    }

    class Void : EtaType()
    class VarBind (val item : OrdinaryType) : EtaType()
    class Return(val value : ExpandedType) : EtaType()
    class FunType (val domain : ExpandedType, val codomain : ExpandedType) : EtaType()
}