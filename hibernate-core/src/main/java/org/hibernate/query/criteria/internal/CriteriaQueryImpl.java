/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import java.util.List;
import java.util.Set;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import org.hibernate.query.criteria.JpaCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaOrder;
import org.hibernate.query.criteria.JpaParameterExpression;
import org.hibernate.query.sqm.produce.spi.ParsingContext;

public class CriteriaQueryImpl<T> extends AbstractQueryImpl<T> implements JpaCriteriaQuery<T> {

	public CriteriaQueryImpl(
			JpaCriteriaBuilder builder,
			ParsingContext parsingContext,
			Class<T> resultType) {
		super( builder, parsingContext, resultType );
	}

	@Override
	public JpaCriteriaQuery<T> select(Selection<? extends T> selection) {
		return null;
	}

	@Override
	public JpaCriteriaQuery<T> multiselect(Selection<?>... selections) {
		return null;
	}

	@Override
	public JpaCriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
		return null;
	}

	@Override
	public JpaCriteriaQuery<T> orderBy(Order... o) {
		return null;
	}

	@Override
	public JpaCriteriaQuery<T> orderBy(List<Order> o) {
		return null;
	}

	@Override
	public List<JpaOrder> getJpaOrderList() {
		return null;
	}

	@Override
	public Set<JpaParameterExpression<?>> getJpaParameters() {
		return null;
	}

	@Override
	public List<Order> getOrderList() {
		return null;
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
		return null;
	}

	@Override
	public JpaCriteriaQuery<T> where(Expression<Boolean> restriction) {
		return (JpaCriteriaQuery<T>) super.where( restriction );
	}

	@Override
	public JpaCriteriaQuery<T> where(Predicate... restrictions) {
		return (JpaCriteriaQuery<T>) super.where( restrictions );
	}

	@Override
	public JpaCriteriaQuery<T> groupBy(Expression<?>... grouping) {
		return (JpaCriteriaQuery<T>) super.groupBy( grouping );
	}

	@Override
	public JpaCriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
		return (JpaCriteriaQuery<T>) super.groupBy( grouping );
	}

	@Override
	public JpaCriteriaQuery<T> having(Expression<Boolean> restriction) {
		return (JpaCriteriaQuery<T>) super.having( restriction );
	}

	@Override
	public JpaCriteriaQuery<T> having(Predicate... restrictions) {
		return (JpaCriteriaQuery<T>) super.having( restrictions );
	}

	@Override
	public JpaCriteriaQuery<T> distinct(boolean distinct) {
		return (JpaCriteriaQuery<T>) super.distinct( distinct );
	}
}
