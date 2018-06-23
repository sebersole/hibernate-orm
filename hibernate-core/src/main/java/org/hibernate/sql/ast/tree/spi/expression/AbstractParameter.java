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
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.results.internal.SqlSelectionImpl;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractParameter implements GenericParameter {

	// todo (6.0) (domain-jdbc) : ...

	private final JdbcValueMapper valueMapper;

	public AbstractParameter(JdbcValueMapper valueMapper) {
		this.valueMapper = valueMapper;
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
				jdbcParameterBindings.getBindValue( this ),
				position,
				executionContext
		);
	}

	@Override
	public SqlSelection createSqlSelection(int jdbcPosition) {
		// todo (6.0) : we should really just access the parameter bind value here rather than reading from ResultSet
		//		should be more performant - double so if we can resolve the bind here
		//		and encode it into the SqlSelectionReader
		//
		//		see `org.hibernate.sql.ast.tree.spi.expression.AbstractLiteral.createSqlSelection`

		return new SqlSelectionImpl(
				jdbcPosition,
				this,
				getJdbcValueMapper()
		);
	}
}
