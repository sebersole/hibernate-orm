/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.criteria.JpaCriteriaBuilder;
import org.hibernate.query.criteria.JpaFrom;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmAttributeReference;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmQualifiedJoin;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;

public class JpaJoinImpl<Z, X> extends SqmAttributeJoin implements JpaJoinImplementor<Z, X> {

	private final QueryContext queryContext;
	private final JpaFrom<Z, X> correlationParent;

	public JpaJoinImpl(
			SqmFrom lhs,
			SqmAttributeReference attributeBinding,
			String uid,
			String alias,
			EntityDescriptor intrinsicSubclassIndicator,
			SqmJoinType joinType,
			boolean fetched,
			QueryContext queryContext) {
		super( lhs, attributeBinding, uid, alias, intrinsicSubclassIndicator, joinType, fetched );
		this.queryContext = queryContext;
		this.correlationParent = null;
	}

	public JpaJoinImpl(
			SqmFrom lhs,
			SqmAttributeReference attributeBinding,
			String uid,
			String alias,
			EntityDescriptor intrinsicSubclassIndicator,
			SqmJoinType joinType,
			boolean fetched,
			QueryContext queryContext,
			JpaFrom<Z, X> correlationParent) {
		super( lhs, attributeBinding, uid, alias, intrinsicSubclassIndicator, joinType, fetched );
		this.queryContext = queryContext;
		this.correlationParent = correlationParent;
	}

	@Override
	public JpaFrom<Z, X> getCorrelationParent() {
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
