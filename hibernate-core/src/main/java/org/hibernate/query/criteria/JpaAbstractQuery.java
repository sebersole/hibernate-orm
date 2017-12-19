/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.List;
import java.util.Set;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;

/**
 * Hibernate extensions to the JPA AbstractQuery.
 *
 * @author Christian Beikov
 */
public interface JpaAbstractQuery<T> extends AbstractQuery<T>, JpaCommonAbstractCriteria {

	<X> JpaRoot<X> from(Class<X> entityClass);

	<X> JpaRoot<X> from(EntityType<X> entity);

	JpaAbstractQuery<T> where(Expression<Boolean> restriction);

	JpaAbstractQuery<T> where(Predicate... restrictions);

	JpaAbstractQuery<T> groupBy(Expression<?>... grouping);

	JpaAbstractQuery<T> groupBy(List<Expression<?>> grouping);

	JpaAbstractQuery<T> having(Expression<Boolean> restriction);

	JpaAbstractQuery<T> having(Predicate... restrictions);

	JpaAbstractQuery<T> distinct(boolean distinct);

	Set<JpaRoot<?>> getJpaRoots();

	JpaSelection<T> getSelection();
	List<JpaExpression<?>> getJpaGroupList();

	JpaPredicate getGroupRestriction();
}
