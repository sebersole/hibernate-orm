/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import org.hibernate.query.criteria.JpaFrom;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;

public class JpaRootImpl<T> extends SqmRoot implements JpaRootImplementor<T> {

	private final QueryContext queryContext;
	private final JpaFrom<T, T> correlationParent;

	public JpaRootImpl(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityValuedExpressableType entityReference,
			QueryContext queryContext) {
		super( fromElementSpace, uid, alias, entityReference );
		this.queryContext = queryContext;
		this.correlationParent = null;
	}

	public JpaRootImpl(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityValuedExpressableType entityReference,
			QueryContext queryContext,
			JpaFrom<T, T> correlationParent) {
		super( fromElementSpace, uid, alias, entityReference );
		this.queryContext = queryContext;
		this.correlationParent = correlationParent;
	}

	@Override
	public JpaFrom<T, T> getCorrelationParent() {
		return correlationParent;
	}

	@Override
	public boolean isCorrelated() {
		return correlationParent != null;
	}

	@Override
	public JpaPath<?> getParentPath() {
		return null;
	}

	@Override
	public QueryContext context() {
		return queryContext;
	}
}
