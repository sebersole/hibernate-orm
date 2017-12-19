/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.expression;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;

import org.hibernate.query.criteria.JpaCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaIn;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * Implementor of JpaExpression.
 *
 * @author Christian Beikov
 */
public interface JpaExpressionImplementor<X> extends SqmExpression, JpaExpression<X> {

	QueryContext context();

	@SuppressWarnings("unchecked")
	default Class<? extends X> getJavaType() {
		return getExpressableType().getJavaType();
	}

	default JpaCriteriaBuilder getCriteriaBuilder() {
		return context().criteriaBuilder();
	}

	default JpaPredicate isNull() {
		return getCriteriaBuilder().isNull( this );
	}

	default JpaPredicate isNotNull() {
		return getCriteriaBuilder().isNotNull( this );
	}

	default JpaPredicate in(Object... values) {
		return getCriteriaBuilder().in( this, values );
	}

	default JpaPredicate in(Expression<?>... values) {
		return getCriteriaBuilder().in( this, values );
	}

	default JpaPredicate in(Collection<?> values) {
		JpaIn<X> in = getCriteriaBuilder().in( this );
		for ( Object value : values ) {
			in.value( (X) value );
		}
		return in;
	}

	default JpaPredicate in(Expression<Collection<?>> values) {
		return getCriteriaBuilder().in( this ).value( (Expression<X>) values );
	}

	default <X> JpaExpression<X> as(Class<X> type) {
		return getCriteriaBuilder().cast( this, type );
	}

	@Override
	default List<JpaSelection<?>> getJpaCompoundSelectionItems() {
		return Collections.emptyList();
	}

	@Override
	default boolean isCompoundSelection() {
		return false;
	}

	@Override
	default List<Selection<?>> getCompoundSelectionItems() {
		return Collections.emptyList();
	}
}
