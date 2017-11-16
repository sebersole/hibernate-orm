/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.Collection;
import javax.persistence.criteria.Expression;

/**
 * Hibernate extensions to the JPA Expression.
 *
 * @author Christian Beikov
 */
public interface JpaExpression<T> extends Expression<T>, JpaSelection<T> {

	JpaPredicate isNull();

	JpaPredicate isNotNull();

	JpaPredicate in(Object... values);

	JpaPredicate in(Expression<?>... values);

	JpaPredicate in(Collection<?> values);

	JpaPredicate in(Expression<Collection<?>> values);

	<X> JpaExpression<X> as(Class<X> type);
}
