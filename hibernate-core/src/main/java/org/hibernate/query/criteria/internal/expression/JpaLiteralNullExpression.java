package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmLiteralNull;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;

public class JpaLiteralNullExpression<T> extends SqmLiteralNull implements JpaExpressionImplementor<T> {

	private final QueryContext queryContext;
	private String alias;

	public JpaLiteralNullExpression(QueryContext queryContext, ExpressableType<T> expressionType) {
		this.queryContext = queryContext;
		impliedType(expressionType);
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
