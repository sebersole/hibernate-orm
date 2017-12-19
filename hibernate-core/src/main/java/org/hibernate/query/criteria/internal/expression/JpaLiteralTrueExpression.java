package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmLiteralTrue;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

public class JpaLiteralTrueExpression extends SqmLiteralTrue implements JpaExpressionImplementor<Boolean> {

	private final QueryContext queryContext;
	private String alias;

	public JpaLiteralTrueExpression(
			BasicValuedExpressableType expressionType,
			QueryContext queryContext) {
		super( expressionType );
		this.queryContext = queryContext;
	}

	@Override
	public QueryContext context() {
		return queryContext;
	}

	@Override
	public JpaSelection<Boolean> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
