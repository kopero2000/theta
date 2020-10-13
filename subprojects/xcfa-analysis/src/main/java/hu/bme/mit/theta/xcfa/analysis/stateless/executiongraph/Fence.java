package hu.bme.mit.theta.xcfa.analysis.stateless.executiongraph;

import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.xcfa.XCFA;

class Fence extends MemoryAccess implements hu.bme.mit.theta.mcm.graphfilter.interfaces.Fence {
    private static int cnt;

    static {
        cnt = 0;
    }

    private final int id;
    private final String type;
    Fence(VarDecl<?> globalVar, XCFA.Process parentProcess, MemoryAccess lastNode, String type) {
        super(globalVar, parentProcess, lastNode);
        this.type = type;
        id = cnt++;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "\"F(" + type + ")_" + id + "\"";
    }
}
