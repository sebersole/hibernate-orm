/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.procedure.internal;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * Standard SqlSelection implementation for procedure/function based
 * queries.
 *
 * @author Steve Ebersole
 */
public class SqlSelectionImpl implements SqlSelection {
	private final SqlExpressable sqlExpressable;
	private final int position;

	public SqlSelectionImpl(SqlExpressable sqlExpressable, int position) {
		this.sqlExpressable = sqlExpressable;
		this.position = position;
	}

	@Override
	public JdbcValueMapper getJdbcValueMapper() {
		return sqlExpressable.getJdbcValueMapper();
	}

	@Override
	public int getValuesArrayPosition() {
		return position;
	}

	@Override
	public void accept(SqlAstWalker interpreter) {
		throw new UnsupportedOperationException(  );
	}
}
