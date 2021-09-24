package hu.bme.mit.theta.xcfa.analysis.interleavings.allinterleavings;

import hu.bme.mit.theta.analysis.LTS;
import hu.bme.mit.theta.xcfa.analysis.common.XcfaAction;
import hu.bme.mit.theta.xcfa.analysis.interleavings.XcfaState;
import hu.bme.mit.theta.xcfa.model.XcfaEdge;
import hu.bme.mit.theta.xcfa.model.XcfaLocation;

import java.util.ArrayList;
import java.util.Collection;

public final class XcfaLts implements LTS<XcfaState<?>, XcfaAction> {
	@Override
	public Collection<XcfaAction> getEnabledActionsFor(final XcfaState<?> state) {
		final Collection<XcfaAction> xcfaActions = new ArrayList<>();
		for (Integer enabledProcess : state.getEnabledProcesses()) {
			final XcfaLocation loc = state.getProcessLocs().get(enabledProcess);
				for (XcfaEdge outgoingEdge : loc.getOutgoingEdges()) {
					final XcfaAction xcfaAction = XcfaAction.create(enabledProcess, outgoingEdge);
					xcfaActions.add(xcfaAction);
				}
		}
		return xcfaActions;
	}

}
