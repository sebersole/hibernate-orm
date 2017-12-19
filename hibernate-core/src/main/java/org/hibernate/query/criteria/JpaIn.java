/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

/**
 * Hibernate extensions to the JPA Fetch.
 *
 * @author Christian Beikov
 */
public interface JpaIn<T> extends CriteriaBuilder.In<T>, JpaPredicate {

	JpaExpression<T> getExpression();

	JpaIn<T> value(T value);

	JpaIn<T> value(Expression<? extends T> value);

}
