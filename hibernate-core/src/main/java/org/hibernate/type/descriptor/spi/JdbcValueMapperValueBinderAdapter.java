/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.spi;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.sql.SqlExpressableType;
import org.hibernate.sql.exec.spi.ExecutionContext;

/**
 * @author Steve Ebersole
 */
public class JdbcValueMapperValueBinderAdapter implements ValueBinder {
	private final SqlExpressableType sqlExpressableType;

	public JdbcValueMapperValueBinderAdapter(SqlExpressableType sqlExpressableType) {
		this.sqlExpressableType = sqlExpressableType;
	}

	@Override
	public int getNumberOfJdbcParametersNeeded() {
		return 1;
	}

	public SqlExpressableType getSqlExpressableType() {
		return sqlExpressableType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void bind(
			PreparedStatement st,
			int position,
			Object value,
			ExecutionContext executionContext) throws SQLException {
		sqlExpressableType.getJdbcValueBinder().bind( st, position, value, executionContext );
	}

	@Override
	@SuppressWarnings("unchecked")
	public void bind(
			PreparedStatement st,
			String name,
			Object value,
			ExecutionContext executionContext) throws SQLException {
		sqlExpressableType.getJdbcValueBinder().bind(
				Util.asCallableStatementForNamedParam( st ),
				name, value,
				executionContext
		);
	}
}
