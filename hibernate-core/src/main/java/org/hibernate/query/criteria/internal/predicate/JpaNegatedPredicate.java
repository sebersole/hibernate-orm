package org.hibernate.query.criteria.internal.predicate;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.predicate.NegatedSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

public class JpaNegatedPredicate extends NegatedSqmPredicate implements JpaPredicateImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaNegatedPredicate(
			SqmPredicate wrappedPredicate,
			QueryContext queryContext) {
		super( wrappedPredicate );
		this.queryContext = queryContext;
	}

	@Override
	public boolean isNegated() {
		return true;
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
