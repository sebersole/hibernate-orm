/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;

/**
 * A literal specified in the source query.
 *
 * @author Steve Ebersole
 */
public class GenericSqlLiteral extends AbstractLiteral {
	public GenericSqlLiteral(Object value, JdbcValueMapper jdbcValueMapper, boolean inSelect) {
		super( value, jdbcValueMapper, inSelect );
	}

	@Override
	public void accept(SqlAstWalker  walker) {
		walker.visitLiteral( this );
	}
}
