/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.List;
import java.util.Set;
import javax.persistence.TupleElement;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

/**
 * Hibernate extensions to the JPA Subquery.
 *
 * @author Christian Beikov
 */
public interface JpaSubquery<T> extends Subquery<T>, JpaAbstractQuery<T>, JpaExpression<T> {
	JpaSubquery<T> select(Expression<T> expression);

	JpaSubquery<T> where(Expression<Boolean> restriction);

	JpaSubquery<T> where(Predicate... restrictions);

	JpaSubquery<T> groupBy(Expression<?>... grouping);

	JpaSubquery<T> groupBy(List<Expression<?>> grouping);

	JpaSubquery<T> having(Expression<Boolean> restriction);

	JpaSubquery<T> having(Predicate... restrictions);

	JpaSubquery<T> distinct(boolean distinct);

	<Y> JpaRoot<Y> correlate(Root<Y> parentRoot);

	<X, Y> JpaJoin<X, Y> correlate(Join<X, Y> parentJoin);

	<X, Y> JpaCollectionJoin<X, Y> correlate(CollectionJoin<X, Y> parentCollection);

	<X, Y> JpaSetJoin<X, Y> correlate(SetJoin<X, Y> parentSet);

	<X, Y> JpaListJoin<X, Y> correlate(ListJoin<X, Y> parentList);

	<X, K, V> JpaMapJoin<X, K, V> correlate(MapJoin<X, K, V> parentMap);

	JpaAbstractQuery<?> getParent();

	JpaCommonAbstractCriteria getContainingQuery();

	JpaExpression<T> getSelection();

	Set<JpaJoin<?, ?>> getJpaCorrelatedJoins();
}
