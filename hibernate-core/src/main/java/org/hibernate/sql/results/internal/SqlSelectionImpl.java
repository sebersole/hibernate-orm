/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * @author Steve Ebersole
 */
public class SqlSelectionImpl implements SqlSelection {
	private final int position;
	private final Expression sqlExpression;
	private final JdbcValueExtractor jdbcValueExtractor;

	public SqlSelectionImpl(int position, Expression sqlExpression, JdbcValueMapper jdbcValueMapper) {
		this( position, sqlExpression, jdbcValueMapper.getJdbcValueExtractor() );
	}

	public SqlSelectionImpl(int position, Expression sqlExpression, JdbcValueExtractor jdbcValueExtractor) {
		this.position = position;
		this.sqlExpression = sqlExpression;
		this.jdbcValueExtractor = jdbcValueExtractor;
	}

	@Override
	public JdbcValueExtractor getJdbcValueExtractor() {
		return jdbcValueExtractor;
	}

	@Override
	public int getValuesArrayPosition() {
		return position;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void accept(SqlAstWalker interpreter) {
		sqlExpression.accept( interpreter );
	}
}
