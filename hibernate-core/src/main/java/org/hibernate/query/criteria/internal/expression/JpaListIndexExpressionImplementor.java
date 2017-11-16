/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexReference;

/**
 * Implementor of JpaExpression for list index.
 *
 * @author Christian Beikov
 */
public interface JpaListIndexExpressionImplementor extends SqmCollectionIndexReference, JpaExpressionImplementor<Integer> {

	@Override
	default Class<? extends Integer> getJavaType() {
		return Integer.class;
	}

}
