/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;

/**
 * @author Steve Ebersole
 */
public interface ParameterSpec<J> extends Expression<J> {
	JdbcValueMapper getJdbcValueMapper();

	void bindValue(
			PreparedStatement preparedStatement,
			JdbcParameterBindings jdbcParameterBindings,
			int position,
			ExecutionContext executionContext) throws SQLException;

	default void bindValue(
			PreparedStatement preparedStatement,
			JdbcParameterBindings jdbcParameterBindings,
			String parameterName,
			ExecutionContext executionContext) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}
}
