/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.spi;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.exec.spi.ExecutionContext;

/**
 * @author Steve Ebersole
 */
public class JdbcValueMapperValueBinderAdapter implements ValueBinder {
	private final JdbcValueMapper jdbcValueMapper;

	public JdbcValueMapperValueBinderAdapter(JdbcValueMapper jdbcValueMapper) {
		this.jdbcValueMapper = jdbcValueMapper;
	}

	@Override
	public int getNumberOfJdbcParametersNeeded() {
		return 1;
	}

	public JdbcValueMapper getJdbcValueMapper() {
		return jdbcValueMapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void bind(
			PreparedStatement st,
			int position,
			Object value,
			ExecutionContext executionContext) throws SQLException {
		jdbcValueMapper.getJdbcValueBinder().bind( st, position, value, executionContext );
	}

	@Override
	@SuppressWarnings("unchecked")
	public void bind(
			PreparedStatement st,
			String name,
			Object value,
			ExecutionContext executionContext) throws SQLException {
		jdbcValueMapper.getJdbcValueBinder().bind(
				Util.asCallableStatementForNamedParam( st ),
				name, value,
				executionContext
		);
	}
}
