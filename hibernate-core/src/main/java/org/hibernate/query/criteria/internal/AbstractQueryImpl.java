/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.hibernate.query.criteria.JpaAbstractQuery;
import org.hibernate.query.criteria.JpaCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.path.JpaRootImpl;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;

public abstract class AbstractQueryImpl<T> extends CommonAbstractCriteriaImpl implements JpaAbstractQuery<T> {

	private final Class<T> resultType;
	private final SqmQuerySpec querySpec;
	private Set<JpaRoot<?>> jpaRoots;

	public AbstractQueryImpl(JpaCriteriaBuilder builder, ParsingContext parsingContext, Class<T> resultType) {
		super( builder, parsingContext );
		this.resultType = resultType;
		this.querySpec = new SqmQuerySpec();
	}

	@Override
	public Class<T> getResultType() {
		return resultType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> JpaRoot<X> from(Class<X> entityClass) {
		JpaRootImpl<X> root = createRoot( entityClass );
		querySpec.getFromClause().makeFromElementSpace().setRoot( root );
		return root;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> JpaRoot<X> from(EntityType<X> entity) {
		JpaRootImpl<X> root = createRoot( entity );
		querySpec.getFromClause().makeFromElementSpace().setRoot( root );
		return root;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Root<?>> getRoots() {
		return (Set<Root<?>>) (Set<?>) getJpaRoots();
	}

	@Override
	public Set<JpaRoot<?>> getJpaRoots() {
		if (jpaRoots == null) {
			this.jpaRoots = new AbstractSet<JpaRoot<?>>() {
				@Override
				public Iterator<JpaRoot<?>> iterator() {
					return new Iterator<JpaRoot<?>>() {

						final Iterator<SqmFromElementSpace> fromElementSpaceIterator = querySpec.getFromClause()
								.getFromElementSpaces().iterator();

						@Override
						public boolean hasNext() {
							return fromElementSpaceIterator.hasNext();
						}

						@Override
						public JpaRoot<?> next() {
							return (JpaRoot<?>) fromElementSpaceIterator.next();
						}
					};
				}

				@Override
				public int size() {
					return querySpec.getFromClause().getFromElementSpaces().size();
				}
			};
		}
		return jpaRoots;
	}

	@Override
	public JpaSelection<T> getSelection() {
		return null;
	}

	@Override
	public JpaAbstractQuery<T> distinct(boolean distinct) {
		querySpec.getSelectClause().setDistinct(distinct);
		return this;
	}

	@Override
	public boolean isDistinct() {
		return querySpec.getSelectClause().isDistinct();
	}

	@Override
	protected SqmWhereClause getWhereClause() {
		return querySpec.getWhereClause();
	}

	@Override
	public JpaAbstractQuery<T> where(Expression<Boolean> restriction) {
		setWhere( querySpec.getWhereClause(), restriction );
		return this;
	}

	@Override
	public JpaAbstractQuery<T> where(Predicate... restrictions) {
		setWhere( querySpec.getWhereClause(), restrictions );
		return this;
	}

	@Override
	public JpaAbstractQuery<T> groupBy(Expression<?>... grouping) {
		return null;
	}

	@Override
	public JpaAbstractQuery<T> groupBy(List<Expression<?>> grouping) {
		return null;
	}

	@Override
	public JpaAbstractQuery<T> having(Expression<Boolean> restriction) {
		return null;
	}

	@Override
	public JpaAbstractQuery<T> having(Predicate... restrictions) {
		return null;
	}

	@Override
	public List<JpaExpression<?>> getJpaGroupList() {
		return null;
	}

	@Override
	public JpaPredicate getGroupRestriction() {
		return null;
	}

	@Override
	public List<Expression<?>> getGroupList() {
		return null;
	}
}
