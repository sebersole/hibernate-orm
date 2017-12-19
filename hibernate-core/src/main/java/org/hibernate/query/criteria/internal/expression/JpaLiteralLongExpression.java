package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmLiteralLong;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

public class JpaLiteralLongExpression extends SqmLiteralLong implements JpaExpressionImplementor<Long> {

	private final QueryContext queryContext;
	private String alias;

	public JpaLiteralLongExpression(
			Long value,
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
	public JpaSelection<Long> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
