package hu.bme.mit.theta.xcfa.analysis.stateless;

import hu.bme.mit.theta.core.model.MutableValuation;
import hu.bme.mit.theta.core.stmt.AssumeStmt;
import hu.bme.mit.theta.core.stmt.Stmt;
import hu.bme.mit.theta.core.type.booltype.BoolLitExpr;
import hu.bme.mit.theta.xcfa.XCFA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {

    private final MutableValuation mutableValuation;
    private final XCFA xcfa;
    private final Map<XCFA.Process, List<XCFA.Process.Procedure>> callStacks;
    private final Map<XCFA.Process, XCFA.Process.Procedure.Location> currentLocs;
    private XCFA.Process currentlyAtomic;

    public State(final XCFA xcfa) {
        this.mutableValuation = new MutableValuation();
        this.xcfa = xcfa;
        this.callStacks = new HashMap<>();
        this.currentLocs = new HashMap<>();
        for(XCFA.Process process : xcfa.getProcesses()) {
            this.callStacks.put(process, new ArrayList<>());
            this.currentLocs.put(process, process.getMainProcedure().getInitLoc());
        }
        currentlyAtomic = null;
    }

    private State(
            final MutableValuation mutableValuation,
            final XCFA xcfa,
            final Map<XCFA.Process, List<XCFA.Process.Procedure>> callStacks,
            final Map<XCFA.Process, XCFA.Process.Procedure.Location> currentLocs,
            final XCFA.Process currentlyAtomic
            ) {
        this.mutableValuation = MutableValuation.copyOf(mutableValuation);
        this.xcfa = xcfa;
        this.callStacks = new HashMap<>();
        for(XCFA.Process process : xcfa.getProcesses()) {
            this.callStacks.put(process, List.copyOf(callStacks.get(process)));
        }
        this.currentLocs =  Map.copyOf(currentLocs);
        this.currentlyAtomic = currentlyAtomic;
    }

    public static State copyOf(State state) {
        return new State(state.mutableValuation, state.xcfa, state.callStacks, state.currentLocs, state.currentlyAtomic);
    }

    public MutableValuation getMutableValuation() {
        return mutableValuation;
    }

    public XCFA getXcfa() {
        return xcfa;
    }

    public Map<XCFA.Process, List<XCFA.Process.Procedure>> getCallStacks() {
        return callStacks;
    }

    public Map<XCFA.Process, XCFA.Process.Procedure.Location> getCurrentLocs() {
        return currentLocs;
    }

    public XCFA.Process getCurrentlyAtomic() {
        return currentlyAtomic;
    }

    public void setCurrentlyAtomic(XCFA.Process currentlyAtomic) {
        this.currentlyAtomic = currentlyAtomic;
    }

    public XCFA.Process.Procedure.Edge getOneStep() {
        for(XCFA.Process process : xcfa.getProcesses()) {
            for(XCFA.Process.Procedure.Edge edge : currentLocs.get(process).getOutgoingEdges()) {
                boolean disabled = false;
                for(Stmt stmt : edge.getStmts()) {
                    if(stmt instanceof AssumeStmt) {
                        if(((BoolLitExpr)((AssumeStmt) stmt).getCond().eval(mutableValuation)).getValue()) {
                            return edge;
                        }
                        else {
                            disabled = true;
                            break;
                        }
                    }
                }
                if(!disabled) return edge;
            }
        }
        return null;
    }

}
