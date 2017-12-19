package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexReferenceEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;

public class JpaListIndexExpressionEmbedded extends SqmCollectionIndexReferenceEmbedded implements
		JpaListIndexExpressionImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaListIndexExpressionEmbedded(
			SqmPluralAttributeReference pluralAttributeBinding,
			QueryContext queryContext) {
		super( pluralAttributeBinding );
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

	@Override
	public void setIdentificationVariable(String identificationVariable) {
		this.alias = identificationVariable;
	}
}
