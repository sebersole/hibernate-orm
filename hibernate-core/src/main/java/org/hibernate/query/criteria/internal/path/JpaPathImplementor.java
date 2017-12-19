/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.internal.expression.JpaExpressionImplementor;
import org.hibernate.query.criteria.internal.expression.JpaTypeExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;

/**
 * Implementor of JpaPath.
 *
 * @author Christian Beikov
 */
public interface JpaPathImplementor<X> extends JpaPath<X>, JpaExpressionImplementor<X> {

	SqmNavigableReference getNavigableReference();

	@Override
	@SuppressWarnings("unchecked")
	default Class<? extends X> getJavaType() {
		return getNavigableReference().getJavaType();
	}

	default JpaExpression<Class<? extends X>> type() {
		return new JpaTypeExpression<>( getNavigableReference(), context() );
	}
}
