/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.spi;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.ResultSetMappingDescriptor;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.sql.results.spi.SqlSelectionReader;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Implementation of SqlSelection for native-SQL queries
 * adding "auto discovery" capabilities.
 *
 * @author Steve Ebersole
 */
public class ResolvingSqlSelectionImpl implements SqlSelection, SqlSelectionReader {
	private final String columnAlias;
	private JdbcValueMapper jdbcValueMapper;

	private Integer jdbcResultSetPosition;

	@SuppressWarnings("unused")
	public ResolvingSqlSelectionImpl(String columnAlias, int jdbcResultSetPosition) {
		this.columnAlias = columnAlias;
		this.jdbcResultSetPosition = jdbcResultSetPosition;
	}

	public ResolvingSqlSelectionImpl(String columnAlias) {
		this( columnAlias, null );
	}

	public ResolvingSqlSelectionImpl(String columnAlias, JdbcValueMapper jdbcValueMapper) {
		this.columnAlias = columnAlias;
		this.jdbcValueMapper = jdbcValueMapper;
	}

	@Override
	public void prepare(
			ResultSetMappingDescriptor.JdbcValuesMetadata jdbcResultsMetadata,
			SessionFactoryImplementor sessionFactory) {
		// resolve the column-alias to a position
		jdbcResultSetPosition = jdbcResultsMetadata.resolveColumnPosition( columnAlias );

		if ( jdbcValueMapper == null ) {
			// assume we should auto-discover the type
			final SqlTypeDescriptor sqlTypeDescriptor = jdbcResultsMetadata.resolveSqlTypeDescriptor( jdbcResultSetPosition );

			final TypeConfiguration typeConfiguration = sessionFactory.getTypeConfiguration();
			jdbcValueMapper = sqlTypeDescriptor.getJdbcValueMapper(
					sqlTypeDescriptor.getJdbcRecommendedJavaTypeMapping( typeConfiguration ),
					typeConfiguration
			);
		}

	}

	@Override
	public SqlSelectionReader getSqlSelectionReader() {
		return this;
	}

	@Override
	public Object read(
			ResultSet resultSet,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			SqlSelection sqlSelection) throws SQLException {
		validateExtractor();

		return jdbcValueMapper.getJdbcValueExtractor().extract(
				resultSet,
				sqlSelection.getJdbcResultSetIndex(),
				jdbcValuesSourceProcessingState.getExecutionContext()
		);
	}

	private void validateExtractor() {
		if ( jdbcValueMapper == null ) {
			throw new QueryException( "Could not determine how to read JDBC value" );
		}
	}

	@Override
	public Object extractParameterValue(
			CallableStatement statement,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			int jdbcParameterIndex) throws SQLException {
		validateExtractor();

		return jdbcValueMapper.getJdbcValueExtractor().extract(
				statement,
				jdbcParameterIndex,
				jdbcValuesSourceProcessingState.getExecutionContext()
		);
	}

	@Override
	public Object extractParameterValue(
			CallableStatement statement,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			String jdbcParameterName) throws SQLException {
		validateExtractor();

		return jdbcValueMapper.getJdbcValueExtractor().extract(
				statement,
				jdbcParameterName,
				jdbcValuesSourceProcessingState.getExecutionContext()
		);
	}

	@Override
	public int getJdbcResultSetIndex() {
		return jdbcResultSetPosition;
	}

	@Override
	public int getValuesArrayPosition() {
		return jdbcResultSetPosition -1;
	}

	@Override
	public void accept(SqlAstWalker interpreter) {
		interpreter.visitSqlSelection( this );
	}
}
