package hu.bme.mit.inf.theta.analysis.sts.pred;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import hu.bme.mit.inf.theta.analysis.TransferFunction;
import hu.bme.mit.inf.theta.analysis.pred.PredPrecision;
import hu.bme.mit.inf.theta.analysis.pred.PredState;
import hu.bme.mit.inf.theta.analysis.sts.StsAction;
import hu.bme.mit.inf.theta.core.expr.Expr;
import hu.bme.mit.inf.theta.core.expr.impl.Exprs;
import hu.bme.mit.inf.theta.core.type.BoolType;
import hu.bme.mit.inf.theta.formalism.common.Valuation;
import hu.bme.mit.inf.theta.formalism.sts.STS;
import hu.bme.mit.inf.theta.formalism.utils.PathUtils;
import hu.bme.mit.inf.theta.solver.Solver;

class StsPredTransferFunction implements TransferFunction<PredState, StsAction, PredPrecision> {

	private final Collection<Expr<? extends BoolType>> invar;
	private final Solver solver;

	StsPredTransferFunction(final STS sts, final Solver solver) {
		this.invar = checkNotNull(sts.getInvar());
		this.solver = checkNotNull(solver);
	}

	@Override
	public Collection<PredState> getSuccStates(final PredState state, final StsAction action,
			final PredPrecision precision) {
		checkNotNull(state);
		checkNotNull(action);
		checkNotNull(precision);

		final Set<PredState> succStates = new HashSet<>();
		solver.push();
		solver.add(PathUtils.unfold(state.toExpr(), 0));
		action.getTrans().stream().forEach(t -> solver.add(PathUtils.unfold(t, 0)));
		// TODO: optimization: cache the unfolded invar for 0 and 1
		invar.stream().forEach(i -> solver.add(PathUtils.unfold(i, 0)));
		invar.stream().forEach(i -> solver.add(PathUtils.unfold(i, 1)));
		boolean moreSuccStates;
		do {
			moreSuccStates = solver.check().boolValue();
			if (moreSuccStates) {
				final Valuation nextSuccStateVal = PathUtils.extractValuation(solver.getModel(), 1);

				final PredState nextSuccState = precision.mapToAbstractState(nextSuccStateVal);
				succStates.add(nextSuccState);
				solver.add(PathUtils.unfold(Exprs.Not(nextSuccState.toExpr()), 1));
			}
		} while (moreSuccStates);
		solver.pop();

		return succStates;
	}

}