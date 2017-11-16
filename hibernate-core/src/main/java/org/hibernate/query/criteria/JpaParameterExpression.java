/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.ParameterExpression;

/**
 * Hibernate extensions to the JPA ParameterExpression.
 *
 * @author Christian Beikov
 */
public interface JpaParameterExpression<T> extends ParameterExpression<T>, JpaExpression<T> {
}
