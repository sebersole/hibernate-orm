/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.predicate;

import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.Expression;

import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.internal.expression.JpaExpressionImplementor;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

/**
 * Implementor of JpaPredicate.
 *
 * @author Christian Beikov
 */
public interface JpaPredicateImplementor extends JpaPredicate, JpaExpressionImplementor<Boolean>, SqmPredicate {

	@Override
	default Class<? extends Boolean> getJavaType() {
		return Boolean.class;
	}

	@Override
	default JpaPredicate not() {
		return new JpaNegatedPredicate( this, context() );
	}

	@Override
	default BooleanOperator getOperator() {
		return BooleanOperator.AND;
	}

	@Override
	@SuppressWarnings("unchecked")
	default List<JpaExpression<Boolean>> getJpaExpressions() {
		return Collections.EMPTY_LIST;
	}

	@Override
	@SuppressWarnings("unchecked")
	default List<Expression<Boolean>> getExpressions() {
		return Collections.EMPTY_LIST;
	}
}
