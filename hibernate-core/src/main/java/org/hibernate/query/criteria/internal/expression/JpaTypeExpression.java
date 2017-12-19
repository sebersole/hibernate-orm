/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.expression;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.internal.QueryContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityTypeExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;

public class JpaTypeExpression<X> extends SqmEntityTypeExpression implements JpaExpressionImplementor<X> {

	private final QueryContext queryContext;
	private String alias;

	public JpaTypeExpression(
			SqmNavigableReference binding,
			QueryContext queryContext) {
		super( binding );
		this.queryContext = queryContext;
	}

	@Override
	public QueryContext context() {
		return queryContext;
	}

	@Override
	public JpaSelection<X> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
