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
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * @author Steve Ebersole
 */
public class LiteralParameterInjectable implements GenericParameter {
	private final JdbcValueMapper valueMapper;

	// todo (6.0) (domain-jdbc) : maybe remove
	//		considering the paradigm of multiple executions through MultiSetJdbcValueBindings,
	// 		this impl would no longer serve any purpose

	private Object value;

	public LiteralParameterInjectable(JdbcValueMapper valueMapper) {
		this.valueMapper = valueMapper;
	}

	public void injectCurrentValue(Object value) {
		this.value = value;
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitGenericParameter( this );
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
	public JdbcValueMapper getJdbcValueMapper() {
		return valueMapper;
	}

	@Override
	public SqlSelection createSqlSelection(int jdbcPosition) {
		throw new UnsupportedOperationException();
	}
}
