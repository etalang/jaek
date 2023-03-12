package edu.cornell.cs.cs4120.etac.ir;

import edu.cornell.cs.cs4120.etac.ir.visit.InsnMapsBuilder;
import edu.cornell.cs.cs4120.util.SExpPrinter;

/** An intermediate representation for naming a memory address */
public class IRLabel extends IRStmt {
    private String name;

    /** @param name name of this memory address */
    public IRLabel(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String label() {
        return "LABEL(" + name + ")";
    }

    @Override
    public InsnMapsBuilder buildInsnMapsEnter(InsnMapsBuilder v) {
        v.addNameToCurrentIndex(name);
        return v;
    }

    @Override
    public void printSExp(SExpPrinter p) {
        p.startList();
        p.printAtom("LABEL");
        p.printAtom(name);
        p.endList();
    }
}
