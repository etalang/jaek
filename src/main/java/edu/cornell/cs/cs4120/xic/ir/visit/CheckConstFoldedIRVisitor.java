package edu.cornell.cs.cs4120.xic.ir.visit;

import edu.cornell.cs.cs4120.xic.ir.IRNode;

public class CheckConstFoldedIRVisitor extends AggregateVisitor<Boolean> {

    protected IRNode offender;

    public IRNode unfolded() {
        return offender;
    }

    @Override
    public Boolean unit() {
        return true;
    }

    @Override
    public Boolean bind(Boolean r1, Boolean r2) {
        return r1 && r2;
    }

    @Override
    protected Boolean leave(IRNode parent, IRNode n, Boolean r, AggregateVisitor<Boolean> v_) {
        if (!r) return false;
        if (!n.isConstFolded(this)) {
            offender = n;
            return false;
        }
        return true;
    }
}
