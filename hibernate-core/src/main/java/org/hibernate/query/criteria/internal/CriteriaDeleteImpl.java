/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;

import org.hibernate.query.criteria.JpaCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaDelete;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.query.criteria.internal.path.JpaRootImpl;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;

public class CriteriaDeleteImpl<T> extends CommonAbstractCriteriaImpl implements JpaCriteriaDelete<T> {

	private final SqmWhereClause whereClause;
	private JpaRootImpl<T> root;

	public CriteriaDeleteImpl(JpaCriteriaBuilder builder, ParsingContext parsingContext) {
		super( builder, parsingContext );
		this.whereClause = new SqmWhereClause();
	}

	@Override
	@SuppressWarnings("unchecked")
	public JpaRoot<T> from(Class entityClass) {
		JpaRootImpl<T> root = createRoot( entityClass );
		this.root = root;
		return root;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JpaRoot<T> from(EntityType entity) {
		JpaRootImpl<T> root = createRoot( entity );
		this.root = root;
		return root;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JpaRoot<T> getRoot() {
		return root;
	}

	@Override
	public JpaCriteriaDelete<T> where(Expression<Boolean> restriction) {
		setWhere( whereClause, restriction );
		return this;
	}

	@Override
	public JpaCriteriaDelete<T> where(Predicate... restrictions) {
		setWhere( whereClause, restrictions );
		return this;
	}

	@Override
	protected SqmWhereClause getWhereClause() {
		return whereClause;
	}

	@Override
	protected SqmFromElementSpace getFromElementSpace() {
		return null;
	}
}
