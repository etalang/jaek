package edu.cornell.cs.cs4120.xic.ir;

public interface IRExpr extends IRNode {
    boolean isConstant();

    long constant();
}
