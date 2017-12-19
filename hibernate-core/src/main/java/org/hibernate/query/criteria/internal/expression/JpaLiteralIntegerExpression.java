package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmLiteralInteger;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

public class JpaLiteralIntegerExpression extends SqmLiteralInteger implements JpaExpressionImplementor<Integer> {

	private final QueryContext queryContext;
	private String alias;

	public JpaLiteralIntegerExpression(
			Integer value,
			BasicValuedExpressableType sqmExpressableTypeBasic,
			QueryContext queryContext) {
		super( value, sqmExpressableTypeBasic );
		this.queryContext = queryContext;
	}

	public JpaLiteralIntegerExpression(int i, QueryContext queryContext) {
		super( i );
		this.queryContext = queryContext;
	}

	@Override
	public QueryContext context() {
		return queryContext;
	}

	@Override
	public JpaSelection<Integer> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
