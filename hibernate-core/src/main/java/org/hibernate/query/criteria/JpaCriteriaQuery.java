/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

/**
 * Hibernate extensions to the JPA CriteriaQuery.
 *
 * @author Christian Beikov
 */
public interface JpaCriteriaQuery<T> extends CriteriaQuery<T>, JpaAbstractQuery<T> {
	JpaCriteriaQuery<T> select(Selection<? extends T> selection);

	JpaCriteriaQuery<T> multiselect(Selection<?>... selections);

	JpaCriteriaQuery<T> multiselect(List<Selection<?>> selectionList);

	JpaCriteriaQuery<T> where(Expression<Boolean> restriction);

	JpaCriteriaQuery<T> where(Predicate... restrictions);

	JpaCriteriaQuery<T> groupBy(Expression<?>... grouping);

	JpaCriteriaQuery<T> groupBy(List<Expression<?>> grouping);

	JpaCriteriaQuery<T> having(Expression<Boolean> restriction);

	JpaCriteriaQuery<T> having(Predicate... restrictions);

	JpaCriteriaQuery<T> orderBy( Order... o);

	JpaCriteriaQuery<T> orderBy(List<Order> o);

	JpaCriteriaQuery<T> distinct(boolean distinct);

	List<JpaOrder> getJpaOrderList();

	Set<JpaParameterExpression<?>> getJpaParameters();
}
