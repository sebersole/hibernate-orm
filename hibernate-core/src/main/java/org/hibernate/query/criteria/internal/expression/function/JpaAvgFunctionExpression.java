package org.hibernate.query.criteria.internal.expression.function;

import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.criteria.internal.expression.JpaExpressionImplementor;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.function.SqmAvgFunction;

public class JpaAvgFunctionExpression<T> extends SqmAvgFunction implements JpaExpressionImplementor<T> {

	private final QueryContext queryContext;
	private String alias;

	public JpaAvgFunctionExpression(
			SqmExpression argument,
			QueryContext queryContext) {
		super( argument );
		this.queryContext = queryContext;
	}

	public JpaAvgFunctionExpression(
			SqmExpression argument,
			AllowableFunctionReturnType resultType,
			QueryContext queryContext) {
		super( argument, resultType );
		this.queryContext = queryContext;
	}

	@Override
	public QueryContext context() {
		return queryContext;
	}

	@Override
	public JpaSelection<T> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public String getAlias() {
		return alias;
	}

}
