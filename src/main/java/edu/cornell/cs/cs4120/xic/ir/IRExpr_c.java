package edu.cornell.cs.cs4120.xic.ir;

import edu.cornell.cs.cs4120.xic.ir.visit.CheckCanonicalIRVisitor;

/** An intermediate representation for expressions */
public abstract class IRExpr_c extends IRNode_c implements IRExpr {

    @Override
    public CheckCanonicalIRVisitor checkCanonicalEnter(CheckCanonicalIRVisitor v) {
        return v.enterExpr();
    }

    @Override
    public boolean isCanonical(CheckCanonicalIRVisitor v) {
        return v.inExpr() || !v.inExp();
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public long constant() {
        throw new UnsupportedOperationException();
    }
}
