/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.criteria.JpaFrom;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmAttributeReference;
import org.hibernate.query.sqm.tree.from.SqmFrom;

public class JpaSetJoinImpl<Z, E> extends JpaJoinImpl<Z, E> implements JpaSetJoinImplementor<Z, E> {

	public JpaSetJoinImpl(
			SqmFrom lhs,
			SqmAttributeReference attributeBinding,
			String uid,
			String alias,
			EntityDescriptor intrinsicSubclassIndicator,
			SqmJoinType joinType,
			boolean fetched,
			QueryContext queryContext) {
		super( lhs, attributeBinding, uid, alias, intrinsicSubclassIndicator, joinType, fetched, queryContext );
	}

	public JpaSetJoinImpl(
			SqmFrom lhs,
			SqmAttributeReference attributeBinding,
			String uid,
			String alias,
			EntityDescriptor intrinsicSubclassIndicator,
			SqmJoinType joinType,
			boolean fetched,
			QueryContext queryContext,
			JpaFrom<Z, E> correlationParent) {
		super(
				lhs,
				attributeBinding,
				uid,
				alias,
				intrinsicSubclassIndicator,
				joinType,
				fetched,
				queryContext,
				correlationParent
		);
	}
}
