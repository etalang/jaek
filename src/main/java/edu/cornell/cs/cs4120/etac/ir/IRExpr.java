package edu.cornell.cs.cs4120.etac.ir;

public interface IRExpr extends IRNode {
    boolean isConstant();

    long constant();
}
