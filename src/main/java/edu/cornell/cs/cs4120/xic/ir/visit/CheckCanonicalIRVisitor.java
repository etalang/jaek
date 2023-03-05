package edu.cornell.cs.cs4120.xic.ir.visit;

import edu.cornell.cs.cs4120.xic.ir.IRNode;

public class CheckCanonicalIRVisitor extends AggregateVisitor<Boolean> {

    protected boolean inSeq;
    protected boolean inExp;
    protected boolean inExpr;
    protected boolean inMove;

    protected IRNode offender;
    protected CheckCanonicalIRVisitor outer;

    @Override
    public Boolean unit() {
        return true;
    }

    @Override
    public Boolean bind(Boolean r1, Boolean r2) {
        return r1 && r2;
    }

    @Override
    protected CheckCanonicalIRVisitor enter(IRNode parent, IRNode n) {
        return n.checkCanonicalEnter(this);
    }

    @Override
    protected Boolean leave(IRNode parent, IRNode n, Boolean r, AggregateVisitor<Boolean> v_) {
        if (!r) return false;
        if (!n.isCanonical(this)) {
            noncanonical(parent == null ? n : parent);
            return false;
        }
        return true;
    }

    /**
     * Record that a SEQ is being entered.
     *
     * @return the updated visitor
     */
    public CheckCanonicalIRVisitor enterSeq() {
        CheckCanonicalIRVisitor v = this;
        if (!v.inSeq) {
            v = copyIfNeeded(v);
            v.outer = this;
            v.inSeq = true;
        }
        return v;
    }

    /**
     * Record that an EXP is being entered.
     *
     * @return the updated visitor
     */
    public CheckCanonicalIRVisitor enterExp() {
        CheckCanonicalIRVisitor v = this;
        if (!v.inExp) {
            v = copyIfNeeded(v);
            v.outer = this;
            v.inExp = true;
        }
        return v;
    }

    /**
     * Record that an IR expression is being entered.
     *
     * @return the updated visitor
     */
    public CheckCanonicalIRVisitor enterExpr() {
        CheckCanonicalIRVisitor v = this;
        if (!v.inExpr) {
            v = copyIfNeeded(v);
            v.outer = this;
            v.inExpr = true;
        }
        return v;
    }

    /**
     * Record that a MOVE is being entered.
     *
     * @return the updated visitor
     */
    public CheckCanonicalIRVisitor enterMove() {
        CheckCanonicalIRVisitor v = this;
        if (!v.inMove) {
            v = copyIfNeeded(v);
            v.outer = this;
            v.inMove = true;
        }
        return v;
    }

    /** @return true if the visitor is inside a SEQ; false otherwise */
    public boolean inSeq() {
        return inSeq;
    }

    /** @return true if the visitor is inside an EXP; false otherwise */
    public boolean inExp() {
        return inExp;
    }

    /** @return true if the visitor is inside an expression; false otherwise */
    public boolean inExpr() {
        return inExpr;
    }

    /** @return true if the visitor is inside a MOVE; false otherwise */
    public boolean inMove() {
        return inMove;
    }

    public IRNode noncanonical() {
        return offender;
    }

    public void noncanonical(IRNode offender) {
        this.offender = offender;
        if (outer != null) outer.noncanonical(offender);
    }
}
