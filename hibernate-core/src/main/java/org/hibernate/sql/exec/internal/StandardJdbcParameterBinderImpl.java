/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.sql.SqlExpressableType;
import org.hibernate.sql.exec.ExecutionException;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameter;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcParameterBinding;

/**
 * @author Steve Ebersole
 */
public class StandardJdbcParameterBinderImpl implements JdbcParameterBinder {
	private final JdbcParameter jdbcParameter;

	public StandardJdbcParameterBinderImpl(JdbcParameter jdbcParameter) {
		this.jdbcParameter = jdbcParameter;
	}

	@Override
	public int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			ExecutionContext executionContext) throws SQLException {
		final JdbcParameterBinding binding = executionContext.getJdbcParameterBindings().getBinding( jdbcParameter );
		if ( binding == null ) {
			throw new ExecutionException( "JDBC parameter value not bound - " + this );
		}

		final SqlExpressableType bindType = binding.getBindType();
		if ( bindType == null ) {
			throw new ExecutionException( "JDBC parameter bind type unresolved - " + this );
		}

		bindType.getJdbcValueBinder().bind(
				statement,
				startPosition,
				binding.getBindValue(),
				executionContext
		);

		return 1;
	}
}
