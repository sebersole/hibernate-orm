package org.hibernate.query.criteria.internal.predicate;

import java.util.List;

import javax.persistence.criteria.Expression;

import org.hibernate.query.criteria.JpaIn;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.predicate.InListSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

public class JpaInListPredicate<T> extends InListSqmPredicate implements JpaInImplementor<T> {

	private final QueryContext queryContext;
	private String alias;

	public JpaInListPredicate(
			SqmExpression testExpression,
			QueryContext queryContext) {
		super( testExpression );
		this.queryContext = queryContext;
	}

	public JpaInListPredicate(
			SqmExpression testExpression,
			QueryContext queryContext,
			SqmExpression... listExpressions) {
		super( testExpression, listExpressions );
		this.queryContext = queryContext;
	}

	public JpaInListPredicate(
			SqmExpression testExpression,
			List<SqmExpression> listExpressions,
			QueryContext queryContext) {
		super( testExpression, listExpressions );
		this.queryContext = queryContext;
	}

	public JpaInListPredicate(
			SqmExpression testExpression,
			List<SqmExpression> listExpressions,
			boolean negated,
			QueryContext queryContext) {
		super( testExpression, listExpressions, negated );
		this.queryContext = queryContext;
	}

	@Override
	public JpaIn<T> value(T value) {
		addExpression( (SqmExpression) context().criteriaBuilder().literal( value ) );
		return this;
	}

	@Override
	public JpaIn<T> value(Expression<? extends T> value) {
		addExpression( (SqmExpression) value );
		return this;
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
