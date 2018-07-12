/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.spi;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.exec.spi.ExecutionContext;

/**
 * @author Steve Ebersole
 */
public class JdbcValueMapperValueExtratorAdapter implements ValueExtractor {
	private final JdbcValueMapper jdbcValueMapper;

	public JdbcValueMapperValueExtratorAdapter(JdbcValueMapper jdbcValueMapper) {
		this.jdbcValueMapper = jdbcValueMapper;
	}

	@Override
	public int getNumberOfJdbcParametersNeeded() {
		return 1;
	}

	@Override
	public Object extract(ResultSet rs, int position, ExecutionContext executionContext) throws SQLException {
		return jdbcValueMapper.getJdbcValueExtractor().extract( rs, position, executionContext );
	}

	@Override
	public Object extract(CallableStatement statement, int position, ExecutionContext executionContext) throws SQLException {
		return jdbcValueMapper.getJdbcValueExtractor().extract( statement, position, executionContext );
	}

	@Override
	public Object extract(CallableStatement statement, String name, ExecutionContext executionContext) throws SQLException {
		return jdbcValueMapper.getJdbcValueExtractor().extract( statement, name, executionContext );
	}
}
