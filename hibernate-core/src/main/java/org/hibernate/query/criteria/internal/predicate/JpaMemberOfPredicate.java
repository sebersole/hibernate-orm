package org.hibernate.query.criteria.internal.predicate;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.query.sqm.tree.predicate.MemberOfSqmPredicate;

public class JpaMemberOfPredicate extends MemberOfSqmPredicate implements JpaPredicateImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaMemberOfPredicate(
			SqmExpression expression,
			SqmPluralAttributeReference pluralAttributeReference,
			QueryContext queryContext) {
		super( expression, pluralAttributeReference );
		this.queryContext = queryContext;
	}

	public JpaMemberOfPredicate(
			SqmExpression expression,
			SqmPluralAttributeReference pluralAttributeReference,
			boolean negated,
			QueryContext queryContext) {
		super( expression, pluralAttributeReference, negated );
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
