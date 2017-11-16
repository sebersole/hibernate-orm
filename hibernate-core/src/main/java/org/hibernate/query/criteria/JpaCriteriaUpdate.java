/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Hibernate extensions to the JPA CriteriaUpdate.
 *
 * @author Christian Beikov
 */
public interface JpaCriteriaUpdate<T> extends CriteriaUpdate<T>, JpaCommonAbstractCriteria {
	JpaRoot<T> from(Class<T> entityClass);

	JpaRoot<T> from(EntityType<T> entity);

	JpaRoot<T> getRoot();

	<Y, X extends Y> JpaCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, X value);

	<Y> JpaCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value);

	<Y, X extends Y> JpaCriteriaUpdate<T> set(Path<Y> attribute, X value);

	<Y> JpaCriteriaUpdate<T> set(
			Path<Y> attribute,
			Expression<? extends Y> value);

	JpaCriteriaUpdate<T> set(String attributeName, Object value);

	JpaCriteriaUpdate<T> where(Expression<Boolean> restriction);

	JpaCriteriaUpdate<T> where(Predicate... restrictions);
}
