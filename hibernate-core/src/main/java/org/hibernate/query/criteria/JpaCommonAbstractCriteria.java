/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.CommonAbstractCriteria;

/**
 * Hibernate extensions to the JPA CommonAbstractCriteria.
 *
 * @author Christian Beikov
 */
public interface JpaCommonAbstractCriteria extends CommonAbstractCriteria {

	<U> JpaSubquery<U> subquery(Class<U> type);

	JpaPredicate getRestriction();
}
