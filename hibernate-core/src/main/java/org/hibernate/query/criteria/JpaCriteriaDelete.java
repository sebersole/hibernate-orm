/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;

/**
 * Hibernate extensions to the JPA CriteriaDelete.
 *
 * @author Christian Beikov
 */
public interface JpaCriteriaDelete<T> extends CriteriaDelete<T>, JpaCommonAbstractCriteria {
	JpaRoot<T> from(Class<T> entityClass);

	JpaRoot<T> from(EntityType<T> entity);

	JpaRoot<T> getRoot();

	JpaCriteriaDelete<T> where(Expression<Boolean> restriction);

	JpaCriteriaDelete<T> where(Predicate... restrictions);
}
