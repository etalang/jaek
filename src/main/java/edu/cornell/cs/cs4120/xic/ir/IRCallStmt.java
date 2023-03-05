package edu.cornell.cs.cs4120.xic.ir;

import edu.cornell.cs.cs4120.util.SExpPrinter;
import edu.cornell.cs.cs4120.xic.ir.visit.AggregateVisitor;
import edu.cornell.cs.cs4120.xic.ir.visit.CheckCanonicalIRVisitor;
import edu.cornell.cs.cs4120.xic.ir.visit.IRVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An intermediate representation for a call statement. t_1, t_2, _, t_4 = CALL(e_target, e_1, ...,
 * e_n) where n = n_returns.
 */
public class IRCallStmt extends IRStmt {
    protected IRExpr target;
    protected List<IRExpr> args;
    protected Long n_returns;

    /**
     * @param target address of the code for this function call
     * @param args arguments of this function call
     */
    public IRCallStmt(IRExpr target, Long n_returns, IRExpr... args) {
        this(target, n_returns, Arrays.asList(args));
    }

    /**
     * @param target address of the code for this function call
     * @param args arguments of this function call
     */
    public IRCallStmt(IRExpr target, Long n_returns, List<IRExpr> args) {
        this.target = target;
        this.args = args;
        this.n_returns = n_returns;
    }

    public IRExpr target() {
        return target;
    }

    public List<IRExpr> args() {
        return args;
    }

    public Long n_returns() {
        return n_returns;
    }

    @Override
    public String label() {
        return "CALL_STMT";
    }

    @Override
    public IRNode visitChildren(IRVisitor v) {
        boolean modified = false;

        IRExpr target = (IRExpr) v.visit(this, this.target);
        if (target != this.target) modified = true;

        List<IRExpr> results = new ArrayList<>(args.size());
        for (IRExpr arg : args) {
            IRExpr newExpr = (IRExpr) v.visit(this, arg);
            if (newExpr != arg) modified = true;
            results.add(newExpr);
        }

        if (modified) return v.nodeFactory().IRCallStmt(target, n_returns, results);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(target));
        for (IRExpr arg : args) result = v.bind(result, v.visit(arg));
        return result;
    }

    @Override
    public boolean isCanonical(CheckCanonicalIRVisitor v) {
        return !v.inExpr();
    }

    @Override
    public void printSExp(SExpPrinter p) {
        p.startList();
        p.printAtom("CALL_STMT");
        p.printAtom(Long.toString(n_returns));
        target.printSExp(p);
        for (IRExpr arg : args) arg.printSExp(p);
        p.endList();
    }
}
