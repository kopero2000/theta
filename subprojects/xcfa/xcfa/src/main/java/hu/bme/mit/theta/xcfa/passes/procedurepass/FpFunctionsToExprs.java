/*
 * Copyright 2021 Budapest University of Technology and Economics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.bme.mit.theta.xcfa.passes.procedurepass;

import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.stmt.AssignStmt;
import hu.bme.mit.theta.core.stmt.Stmt;
import hu.bme.mit.theta.core.stmt.xcfa.XcfaCallStmt;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.anytype.RefExpr;
import hu.bme.mit.theta.core.type.fptype.FpAbsExpr;
import hu.bme.mit.theta.core.type.fptype.FpIsNanExpr;
import hu.bme.mit.theta.core.type.fptype.FpMaxExpr;
import hu.bme.mit.theta.core.type.fptype.FpMinExpr;
import hu.bme.mit.theta.core.type.fptype.FpRoundToIntegralExpr;
import hu.bme.mit.theta.core.type.fptype.FpRoundingMode;
import hu.bme.mit.theta.core.type.fptype.FpSqrtExpr;
import hu.bme.mit.theta.core.type.fptype.FpType;
import hu.bme.mit.theta.xcfa.model.XcfaEdge;
import hu.bme.mit.theta.xcfa.model.XcfaMetadata;
import hu.bme.mit.theta.xcfa.model.XcfaProcedure;
import hu.bme.mit.theta.xcfa.transformation.model.types.complex.CComplexType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkState;
import static hu.bme.mit.theta.core.stmt.Stmts.Assign;
import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Ite;
import static hu.bme.mit.theta.core.utils.TypeUtils.cast;

public class FpFunctionsToExprs extends ProcedurePass {
	private static final Map<String, BiFunction<XcfaProcedure.Builder, XcfaCallStmt, Stmt>> handlers = new LinkedHashMap<>();
	private static void addHandler(String[] names, BiFunction<XcfaProcedure.Builder, XcfaCallStmt, Stmt> handler) {
		for (String name : names) {
			handlers.put(name, handler);
		}
	}

	static {
		addHandler(new String[]{"fabs", "fabsf", "fabsl"}, FpFunctionsToExprs::handleFabs);
		addHandler(new String[]{"floor", "floorf", "floorl"}, FpFunctionsToExprs::handleFloor);
		addHandler(new String[]{"fmax", "fmaxf", "fmaxl"}, FpFunctionsToExprs::handleFmax);
		addHandler(new String[]{"fmin", "fminf", "fminl"}, FpFunctionsToExprs::handleFmin);
		addHandler(new String[]{"fmod", "fmodf", "fmodl"}, FpFunctionsToExprs::handleFmod);
		addHandler(new String[]{"sqrt", "sqrtf", "sqrtl"}, FpFunctionsToExprs::handleSqrt);
		addHandler(new String[]{"round", "roundf", "roundl"}, FpFunctionsToExprs::handleRound);
		addHandler(new String[]{"isnan"}, FpFunctionsToExprs::handleIsnan);
	}

	private static Stmt handleIsnan(XcfaProcedure.Builder builder, XcfaCallStmt callStmt) {
		checkState(callStmt.getParams().size() == 2, "Function is presumed to be unary!");
		Expr<?> expr = callStmt.getParams().get(0);
		checkState(expr instanceof RefExpr);
		CComplexType type = CComplexType.getType(expr);
		//noinspection unchecked
		AssignStmt<?> assign = Assign(
				cast((VarDecl<?>) ((RefExpr<?>) expr).getDecl(), type.getSmtType()),
				cast(Ite(FpIsNanExpr.of((Expr<FpType>) callStmt.getParams().get(1)), type.getUnitValue(), type.getNullValue()), type.getSmtType()));
		XcfaMetadata.create(assign.getExpr(), "cType", type);
		return assign;
	}

	private static Stmt handleRound(XcfaProcedure.Builder builder, XcfaCallStmt callStmt) {
		checkState(callStmt.getParams().size() == 2, "Function is presumed to be unary!");
		Expr<?> expr = callStmt.getParams().get(0);
		checkState(expr instanceof RefExpr);
		//noinspection unchecked
		AssignStmt<FpType> assign = Assign((VarDecl<FpType>) ((RefExpr<?>) expr).getDecl(), FpRoundToIntegralExpr.of(FpRoundingMode.RNE, (Expr<FpType>) callStmt.getParams().get(1)));
		XcfaMetadata.create(assign.getExpr(), "cType", CComplexType.getType(expr));
		return assign;
	}

	private static Stmt handleSqrt(XcfaProcedure.Builder builder, XcfaCallStmt callStmt) {
		checkState(callStmt.getParams().size() == 2, "Function is presumed to be unary!");
		Expr<?> expr = callStmt.getParams().get(0);
		checkState(expr instanceof RefExpr);
		//noinspection unchecked
		AssignStmt<FpType> assign = Assign((VarDecl<FpType>) ((RefExpr<?>) expr).getDecl(), FpSqrtExpr.of(FpRoundingMode.RNE, (Expr<FpType>) callStmt.getParams().get(1)));
		XcfaMetadata.create(assign.getExpr(), "cType", CComplexType.getType(expr));
		return assign;
	}

	private static Stmt handleFmod(XcfaProcedure.Builder builder, XcfaCallStmt callStmt) {
		throw new UnsupportedOperationException("Fmod not yet supported!");
	}

	private static Stmt handleFmin(XcfaProcedure.Builder builder, XcfaCallStmt callStmt) {
		checkState(callStmt.getParams().size() == 3, "Function is presumed to be binary!");
		Expr<?> expr = callStmt.getParams().get(0);
		checkState(expr instanceof RefExpr);
		//noinspection unchecked
		AssignStmt<FpType> assign = Assign((VarDecl<FpType>) ((RefExpr<?>) expr).getDecl(), FpMinExpr.of(FpRoundingMode.RNE, (Expr<FpType>) callStmt.getParams().get(1), (Expr<FpType>) callStmt.getParams().get(2)));
		XcfaMetadata.create(assign.getExpr(), "cType", CComplexType.getType(expr));
		return assign;
	}

	private static Stmt handleFmax(XcfaProcedure.Builder builder, XcfaCallStmt callStmt) {
		checkState(callStmt.getParams().size() == 3, "Function is presumed to be binary!");
		Expr<?> expr = callStmt.getParams().get(0);
		checkState(expr instanceof RefExpr);
		//noinspection unchecked
		AssignStmt<FpType> assign = Assign((VarDecl<FpType>) ((RefExpr<?>) expr).getDecl(), FpMaxExpr.of(FpRoundingMode.RNE, (Expr<FpType>) callStmt.getParams().get(1), (Expr<FpType>) callStmt.getParams().get(2)));
		XcfaMetadata.create(assign.getExpr(), "cType", CComplexType.getType(expr));
		return assign;
	}

	private static Stmt handleFloor(XcfaProcedure.Builder builder, XcfaCallStmt callStmt) {
		checkState(callStmt.getParams().size() == 2, "Function is presumed to be unary!");
		Expr<?> expr = callStmt.getParams().get(0);
		checkState(expr instanceof RefExpr);
		//noinspection unchecked
		AssignStmt<FpType> assign = Assign((VarDecl<FpType>) ((RefExpr<?>) expr).getDecl(), FpRoundToIntegralExpr.of(FpRoundingMode.RTZ, (Expr<FpType>) callStmt.getParams().get(1)));
		XcfaMetadata.create(assign.getExpr(), "cType", CComplexType.getType(expr));
		return assign;
	}

	private static Stmt handleFabs(XcfaProcedure.Builder builder, XcfaCallStmt callStmt) {
		checkState(callStmt.getParams().size() == 2, "Function is presumed to be unary!");
		Expr<?> expr = callStmt.getParams().get(0);
		checkState(expr instanceof RefExpr);
		//noinspection unchecked
		AssignStmt<FpType> assign = Assign((VarDecl<FpType>) ((RefExpr<?>) expr).getDecl(), FpAbsExpr.of((Expr<FpType>) callStmt.getParams().get(1)));
		XcfaMetadata.create(assign.getExpr(), "cType", CComplexType.getType(expr));
		return assign;
	}


	@Override
	public XcfaProcedure.Builder run(XcfaProcedure.Builder builder) {
		for (XcfaEdge edge : new ArrayList<>(builder.getEdges())) {
			List<Stmt> newStmts = new ArrayList<>();
			boolean found = false;
			for (Stmt stmt : edge.getStmts()) {
				if(stmt instanceof XcfaCallStmt) {
					if(handlers.containsKey(((XcfaCallStmt) stmt).getProcedure())) {
						newStmts.add(handlers.get(((XcfaCallStmt) stmt).getProcedure()).apply(builder, (XcfaCallStmt) stmt));
						found = true;
					}
				}
				else newStmts.add(stmt);
			}
			if(found) {
				builder.removeEdge(edge);
				builder.addEdge(new XcfaEdge(edge.getSource(), edge.getTarget(), newStmts));
			}
		}
		return builder;
	}

}
