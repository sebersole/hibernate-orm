package org.hibernate.query.criteria.internal.predicate;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.query.sqm.tree.predicate.EmptinessSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.RelationalPredicateOperator;

public class JpaEmptinessPredicate extends EmptinessSqmPredicate implements JpaPredicateImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaEmptinessPredicate(
			SqmPluralAttributeReference expression,
			QueryContext queryContext) {
		super( expression );
		this.queryContext = queryContext;
	}

	public JpaEmptinessPredicate(
			SqmPluralAttributeReference expression,
			boolean negated,
			QueryContext queryContext) {
		super( expression, negated );
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
