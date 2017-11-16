package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexReferenceEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;

public class JpaListIndexExpressionEntity extends SqmCollectionIndexReferenceEntity implements
		JpaListIndexExpressionImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaListIndexExpressionEntity(
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
