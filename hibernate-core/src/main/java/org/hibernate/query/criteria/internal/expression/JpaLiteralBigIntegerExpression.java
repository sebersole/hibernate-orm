package org.hibernate.query.criteria.internal.expression;

import java.math.BigInteger;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmLiteralBigInteger;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

public class JpaLiteralBigIntegerExpression extends SqmLiteralBigInteger implements JpaExpressionImplementor<BigInteger> {

	private final QueryContext queryContext;
	private String alias;

	public JpaLiteralBigIntegerExpression(
			BigInteger value,
			BasicValuedExpressableType sqmExpressableTypeBasic,
			QueryContext queryContext) {
		super( value, sqmExpressableTypeBasic );
		this.queryContext = queryContext;
	}

	@Override
	public QueryContext context() {
		return queryContext;
	}

	@Override
	public JpaSelection<BigInteger> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
