/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.type.spi.BasicType;

/**
 * A literal specified in the source query.
 *
 * @author Steve Ebersole
 */
public class QueryLiteral extends AbstractLiteral {
	public QueryLiteral(Object value, BasicValuedExpressableType expressableType, Clause clause) {
		super( value, expressableType, clause );
	}

	@Override
	public int getNumberOfJdbcParametersNeeded() {
		// todo (6.0) : not sure this is accurate if the literal happens to be associated with a non-insertable or non-updateable Navigable ref
		return getType().getNumberOfJdbcParametersNeeded();
	}

	@Override
	public BasicType getType() {
		return super.getType().getBasicType();
	}

	@Override
	public void accept(SqlAstWalker  walker) {
		walker.visitQueryLiteral( this );
	}
}
