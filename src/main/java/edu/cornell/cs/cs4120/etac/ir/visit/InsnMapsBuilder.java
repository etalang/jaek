package edu.cornell.cs.cs4120.etac.ir.visit;

import edu.cornell.cs.cs4120.etac.ir.IRNode;
import edu.cornell.cs.cs4120.util.InternalCompilerError;

import java.util.HashMap;
import java.util.Map;

public class InsnMapsBuilder extends IRVisitor {
    private Map<String, Long> nameToIndex;
    private Map<Long, IRNode> indexToInsn;

    private long index;

    public InsnMapsBuilder() {
        super(null);
        nameToIndex = new HashMap<>();
        indexToInsn = new HashMap<>();
        index = 0;
    }

    public Map<String, Long> nameToIndex() {
        return nameToIndex;
    }

    public Map<Long, IRNode> indexToInsn() {
        return indexToInsn;
    }

    @Override
    protected IRVisitor enter(IRNode parent, IRNode n) {
        InsnMapsBuilder v = n.buildInsnMapsEnter(this);
        return v;
    }

    @Override
    protected IRNode leave(IRNode parent, IRNode n, IRNode n_, IRVisitor v_) {
        return n_.buildInsnMaps((InsnMapsBuilder) v_);
    }

    public void addInsn(IRNode n) {
        indexToInsn.put(index, n);
        index++;
    }

    public void addNameToCurrentIndex(String name) {
        if (nameToIndex.containsKey(name))
            throw new InternalCompilerError(
                    "Error - encountered " + "duplicate name " + name + " in the IR.");
        nameToIndex.put(name, index);
    }
}
