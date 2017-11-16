package org.hibernate.query.criteria.internal.predicate;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.predicate.LikeSqmPredicate;

public class JpaLikePredicate extends LikeSqmPredicate implements JpaPredicateImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaLikePredicate(
			SqmExpression matchExpression,
			SqmExpression pattern,
			SqmExpression escapeCharacter,
			QueryContext queryContext) {
		super( matchExpression, pattern, escapeCharacter );
		this.queryContext = queryContext;
	}

	public JpaLikePredicate(
			SqmExpression matchExpression,
			SqmExpression pattern,
			SqmExpression escapeCharacter,
			boolean negated,
			QueryContext queryContext) {
		super( matchExpression, pattern, escapeCharacter, negated );
		this.queryContext = queryContext;
	}

	public JpaLikePredicate(
			SqmExpression matchExpression,
			SqmExpression pattern,
			QueryContext queryContext) {
		super( matchExpression, pattern );
		this.queryContext = queryContext;
	}

	public JpaLikePredicate(
			SqmExpression matchExpression,
			SqmExpression pattern,
			boolean negated,
			QueryContext queryContext) {
		super( matchExpression, pattern, null, negated );
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
