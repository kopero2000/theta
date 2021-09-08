package hu.bme.mit.theta.xsts.type;

import com.google.common.base.Preconditions;
import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.type.inttype.IntLitExpr;
import hu.bme.mit.theta.core.type.inttype.IntType;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static hu.bme.mit.theta.core.type.abstracttype.AbstractExprs.Eq;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Or;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;

public final class XstsCustomType implements XstsType<IntType> {
    private final String name;
    private final List<XstsCustomLiteral> literals;

    private XstsCustomType(final String name, final List<XstsCustomLiteral> literals) {
        this.name = name;
        this.literals = literals;
    }

    public static XstsCustomType of(final String name, final List<XstsCustomLiteral> literals) {
        return new XstsCustomType(name, literals);
    }

    public String getName() {
        return name;
    }

    public List<XstsCustomLiteral> getLiterals() {
        return literals;
    }

    public static final class XstsCustomLiteral {
        private final BigInteger intValue;
        private final String name;

        private XstsCustomLiteral(String name, BigInteger intValue) {
            this.name = name;
            this.intValue = intValue;
        }

        public static XstsCustomLiteral of(String name, BigInteger intValue) {
            return new XstsCustomLiteral(name, intValue);
        }

        public BigInteger getIntValue() {
            return intValue;
        }

        public String getName() {
            return name;
        }
    }

    public IntType getType() {
        return Int();
    }

    @Override
    public Expr<BoolType> createBoundExpr(VarDecl<IntType> decl) {
        final Expr<BoolType> expr = Or(literals.stream()
                .map(lit -> Eq(decl.getRef(), Int(lit.getIntValue())))
                .collect(Collectors.toList()));
        return expr;
    }

    @Override
    public String serializeLiteral(LitExpr<IntType> literal) {
        final IntLitExpr intLitExpr = (IntLitExpr) literal;
        return literals.get(intLitExpr.getValue().intValue()).getName();
    }
}
