package edu.cornell.cs.cs4120.xic.ir;

import edu.cornell.cs.cs4120.xic.ir.IRBinOp.OpType;

import java.util.List;
import java.util.Map;

public class IRNodeFactory_c implements IRNodeFactory {

    @Override
    public IRBinOp IRBinOp(OpType type, IRExpr left, IRExpr right) {
        return new IRBinOp(type, left, right);
    }

    @Override
    public IRCall IRCall(IRExpr target, IRExpr... args) {
        return new IRCall(target, args);
    }

    @Override
    public IRCall IRCall(IRExpr target, List<IRExpr> args) {
        return new IRCall(target, args);
    }

    @Override
    public IRCJump IRCJump(IRExpr expr, String trueLabel) {
        return new IRCJump(expr, trueLabel);
    }

    @Override
    public IRCJump IRCJump(IRExpr expr, String trueLabel, String falseLabel) {
        return new IRCJump(expr, trueLabel, falseLabel);
    }

    @Override
    public IRCompUnit IRCompUnit(String name) {
        return new IRCompUnit(name);
    }

    @Override
    public IRCompUnit IRCompUnit(String name, Map<String, IRFuncDecl> functions) {
        return new IRCompUnit(name, functions);
    }

    @Override
    public IRConst IRConst(long value) {
        return new IRConst(value);
    }

    @Override
    public IRESeq IRESeq(IRStmt stmt, IRExpr expr) {
        return new IRESeq(stmt, expr);
    }

    @Override
    public IRExp IRExp(IRExpr expr) {
        return new IRExp(expr);
    }

    @Override
    public IRFuncDecl IRFuncDecl(String name, IRStmt stmt) {
        return new IRFuncDecl(name, stmt);
    }

    @Override
    public IRJump IRJump(IRExpr expr) {
        return new IRJump(expr);
    }

    @Override
    public IRLabel IRLabel(String name) {
        return new IRLabel(name);
    }

    @Override
    public IRMem IRMem(IRExpr expr) {
        return new IRMem(expr);
    }

    @Override
    public IRCallStmt IRCallStmt(IRExpr target, Long n_returns, List<IRExpr> args) {
        return new IRCallStmt(target, n_returns, args);
    }

    @Override
    public IRMove IRMove(IRExpr target, IRExpr expr) {
        return new IRMove(target, expr);
    }

    @Override
    public IRName IRName(String name) {
        return new IRName(name);
    }

    @Override
    public IRReturn IRReturn(List<IRExpr> rets) {
        return new IRReturn(rets);
    }

    @Override
    public IRReturn IRReturn(IRExpr... rets) {
        return new IRReturn(rets);
    }

    @Override
    public IRSeq IRSeq(IRStmt... stmts) {
        return new IRSeq(stmts);
    }

    @Override
    public IRSeq IRSeq(List<IRStmt> stmts) {
        return new IRSeq(stmts);
    }

    @Override
    public IRTemp IRTemp(String name) {
        return new IRTemp(name);
    }
}
