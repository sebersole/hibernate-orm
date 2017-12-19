package org.hibernate.query.criteria.internal.predicate;

import javax.persistence.criteria.Expression;

import org.hibernate.query.criteria.JpaIn;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.SubQuerySqmExpression;
import org.hibernate.query.sqm.tree.predicate.InSubQuerySqmPredicate;
import org.hibernate.query.sqm.tree.predicate.RelationalPredicateOperator;
import org.hibernate.query.sqm.tree.predicate.RelationalSqmPredicate;

public class JpaInSubqueryPredicate<T> extends InSubQuerySqmPredicate implements JpaInImplementor<T> {

	private final QueryContext queryContext;
	private String alias;

	public JpaInSubqueryPredicate(
			SqmExpression testExpression,
			SubQuerySqmExpression subQueryExpression,
			QueryContext queryContext) {
		super( testExpression, subQueryExpression );
		this.queryContext = queryContext;
	}

	public JpaInSubqueryPredicate(
			SqmExpression testExpression,
			SubQuerySqmExpression subQueryExpression,
			boolean negated,
			QueryContext queryContext) {
		super( testExpression, subQueryExpression, negated );
		this.queryContext = queryContext;
	}

	@Override
	public JpaIn<T> value(T value) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public JpaIn<T> value(Expression<? extends T> value) {
		throw new UnsupportedOperationException(  );
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
