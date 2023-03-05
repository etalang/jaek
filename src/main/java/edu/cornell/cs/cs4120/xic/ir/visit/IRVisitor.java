package edu.cornell.cs.cs4120.xic.ir.visit;

import edu.cornell.cs.cs4120.util.Copy;
import edu.cornell.cs.cs4120.util.InternalCompilerError;
import edu.cornell.cs.cs4120.xic.ir.IRNode;
import edu.cornell.cs.cs4120.xic.ir.IRNodeFactory;

public abstract class IRVisitor implements Copy<IRVisitor> {

    protected IRNodeFactory inf;

    public IRVisitor(IRNodeFactory inf) {
        this.inf = inf;
    }

    public IRNodeFactory nodeFactory() {
        return inf;
    }

    /** Recursively traverse the IR subtree rooted at {@code n} */
    public IRNode visit(IRNode parent, IRNode n) {
        if (n == null) return null;

        // Allow the visitor implementation to hijack traversal of n
        IRNode overrideValue = override(parent, n);
        if (overrideValue != null) return overrideValue;

        // Possibly establish a new visitor for handling this node and its descendants
        IRVisitor v2 = enter(parent, n);
        if (v2 == null) throw new InternalCompilerError("IRVisitor.enter() returned null!");

        // Construct a node in which the children have been visited and
        // possibly replaced.
        IRNode n2 = n.visitChildren(v2);
        if (n2 == null) throw new InternalCompilerError("IRVisitor.visitChildren() returned null!");

        // Complete the work of visiting this node, possibly building an
        // entirely new node that should replace the original one. What this
        // does is dependent on the particular visitor.
        IRNode n3 = leave(parent, n, n2, v2);
        if (n3 == null) throw new InternalCompilerError("IRVisitor.leave() returned null!");

        return n3;
    }

    /** Recursively traverse the IR subtree rooted at {@code n} */
    public IRNode visit(IRNode node) {
        return visit(null, node);
    }

    /**
     * Allows to hijack the traversal of a subtree. This function is called by {@link #visit(IRNode,
     * IRNode)} upon entering node {@code n}. If a non-null node {@code n0} is returned, the
     * traversal is stopped and the resulting AST has {@code n0} in place of {@code n}.
     *
     * <p>By default, overriding is inactive.
     */
    protected IRNode override(IRNode parent, IRNode n) {
        return null;
    }

    /**
     * Called upon entering {@code n} during the AST traversal. This allows to perform certain
     * actions, including returning a new Node visitor to be used in the subtree.
     */
    protected IRVisitor enter(IRNode parent, IRNode n) {
        return this;
    }

    /**
     * This method typically does the "real work" of a visitor, which can be
     * done by calling a method on the node. It is called after finishing
     * traversal of the subtree rooted at {@code n}, so it receives an input
     * node in which the children have already been visited and possibly replaced.
     *
     * @param parent The parent AST node of {@code n} or {@code null} when it is the root.
     * @param n The original node in the input AST
     * @param n2 The node returned by {@link IRNode#visitChildren(IRVisitor)}, which should
     *    look like n except that the children have been visited.
     * @param v2 The new node visitor created by {@link #enter(IRNode, IRNode)}, or {@code this}.
     */
    protected IRNode leave(IRNode parent, IRNode n, IRNode n2, IRVisitor v2) {
        return n2;
    }

    /**
     * Return a clone of this visitor if the given visitor is this visitor, or the given visitor
     * otherwise.
     *
     * @param v the visitor
     * @return a clone of v if v == this, or v otherwise
     */
    protected <V extends IRVisitor> V copyIfNeeded(V v) {
        if (v == this) return edu.cornell.cs.cs4120.util.Copy.Util.copy(v);
        return v;
    }

    @Override
    public IRVisitor copy() {
        try {
            return (IRVisitor) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }
}
