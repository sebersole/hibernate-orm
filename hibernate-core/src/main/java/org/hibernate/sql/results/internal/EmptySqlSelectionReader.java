/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.sql.CallableStatement;
import java.sql.ResultSet;

import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.sql.results.spi.SqlSelectionReader;

/**
 * @author Steve Ebersole
 */
public class EmptySqlSelectionReader implements SqlSelectionReader {
	/**
	 * Singleton access
	 */
	public static final EmptySqlSelectionReader INSTANCE = new EmptySqlSelectionReader();

	@Override
	public Object read(
			ResultSet resultSet,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			SqlSelection sqlSelection) {
		return null;
	}

	@Override
	public Object extractParameterValue(
			CallableStatement statement,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			int jdbcParameterIndex) {
		return null;
	}

	@Override
	public Object extractParameterValue(
			CallableStatement statement,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			String jdbcParameterName) {
		return null;
	}
}
