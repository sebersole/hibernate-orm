package org.hibernate.query.criteria.internal.predicate;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.predicate.RelationalPredicateOperator;
import org.hibernate.query.sqm.tree.predicate.RelationalSqmPredicate;

public class JpaRelationalPredicate extends RelationalSqmPredicate implements JpaPredicateImplementor {

	private final QueryContext queryContext;
	private String alias;

	public JpaRelationalPredicate(
			RelationalPredicateOperator operator,
			SqmExpression leftHandExpression,
			SqmExpression rightHandExpression,
			QueryContext queryContext) {
		super( operator, leftHandExpression, rightHandExpression );
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
