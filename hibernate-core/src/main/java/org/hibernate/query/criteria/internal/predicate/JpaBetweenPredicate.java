package org.hibernate.query.criteria.internal.predicate;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.predicate.BetweenSqmPredicate;

public class JpaBetweenPredicate extends BetweenSqmPredicate implements JpaPredicateImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaBetweenPredicate(
			SqmExpression expression,
			SqmExpression lowerBound,
			SqmExpression upperBound,
			boolean negated,
			QueryContext queryContext) {
		super( expression, lowerBound, upperBound, negated );
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
