package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmLiteralDouble;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

public class JpaLiteralDoubleExpression extends SqmLiteralDouble implements JpaExpressionImplementor<Double> {

	private final QueryContext queryContext;
	private String alias;

	public JpaLiteralDoubleExpression(
			Double value,
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
	public JpaSelection<Double> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
