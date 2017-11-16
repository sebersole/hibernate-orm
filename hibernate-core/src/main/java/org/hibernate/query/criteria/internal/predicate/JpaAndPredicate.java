package org.hibernate.query.criteria.internal.predicate;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.predicate.AndSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

public class JpaAndPredicate extends AndSqmPredicate implements JpaPredicateImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaAndPredicate(
			SqmPredicate leftHandPredicate,
			SqmPredicate rightHandPredicate,
			QueryContext queryContext) {
		super( leftHandPredicate, rightHandPredicate );
		this.queryContext = queryContext;
	}

	@Override
	public boolean isNegated() {
		return false;
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
