package edu.cornell.cs.cs4120.xic.ir;

import java.util.Arrays;

/** Static data. */
public final class IRData {
    private final String name;
    private final long[] data;

    public IRData(String name, long[] data) {
        this.name = name;
        this.data = data;
    }

    public String name() {
        return name;
    }

    public long[] data() {
        return data;
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof IRData)) return false;
        IRData otherData = (IRData) other;
        return name.equals(otherData.name) && Arrays.equals(data, otherData.data);
    }

    public int hashCode() {
        return name.hashCode() * 31 + Arrays.hashCode(data);
    }
}
