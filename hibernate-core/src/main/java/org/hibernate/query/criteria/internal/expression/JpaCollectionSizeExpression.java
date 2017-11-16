package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmCollectionSize;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

public class JpaCollectionSizeExpression extends SqmCollectionSize implements JpaExpressionImplementor<Integer> {

	private final QueryContext queryContext;
	private String alias;

	public JpaCollectionSizeExpression(
			SqmPluralAttributeReference pluralAttributeBinding,
			BasicValuedExpressableType sizeType,
			QueryContext queryContext) {
		super( pluralAttributeBinding, sizeType );
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
