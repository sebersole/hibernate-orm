package org.hibernate.query.criteria.internal.expression;

import java.math.BigDecimal;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmLiteralBigDecimal;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

public class JpaLiteralBigDecimalExpression extends SqmLiteralBigDecimal implements JpaExpressionImplementor<BigDecimal> {

	private final QueryContext queryContext;
	private String alias;

	public JpaLiteralBigDecimalExpression(
			BigDecimal value,
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
	public JpaSelection<BigDecimal> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
