/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * @author Steve Ebersole
 */
public class LiteralParameter implements GenericParameter, SqlExpressable {
	private final Object value;
	private final JdbcValueMapper valueMapper;

	public LiteralParameter(Object value, JdbcValueMapper type) {
		this.value = value;
		this.valueMapper = type;
	}

	@Override
	public JdbcValueMapper getJdbcValueMapper() {
		return valueMapper;
	}

	@Override
	public void bindValue(
			PreparedStatement preparedStatement,
			JdbcParameterBindings jdbcParameterBindings,
			int position,
			ExecutionContext executionContext) throws SQLException {
		getJdbcValueMapper().getJdbcValueBinder().bind(
				preparedStatement,
				value,
				position,
				executionContext
		);
	}

	@Override
	public SqlSelection createSqlSelection(int jdbcPosition) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitGenericParameter( this );
	}
}
