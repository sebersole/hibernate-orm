/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.spi;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.results.spi.ResultSetMappingDescriptor;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * Implementation of SqlSelection for native-SQL queries
 * adding "auto discovery" capabilities.
 *
 * @author Steve Ebersole
 */
public class ResolvingSqlSelectionImpl implements SqlSelection, SqlExpressable {
	private final String columnAlias;
	private JdbcValueMapper valueMapper;

	private Integer jdbcResultSetPosition;

	public ResolvingSqlSelectionImpl(String columnAlias, int jdbcResultSetPosition) {
		this.columnAlias = columnAlias;
		this.jdbcResultSetPosition = jdbcResultSetPosition;
	}

	public ResolvingSqlSelectionImpl(String columnAlias) {
		this( columnAlias, null );
	}

	public ResolvingSqlSelectionImpl(String columnAlias, JdbcValueMapper valueMapper) {
		this.columnAlias = columnAlias;
		this.valueMapper = valueMapper;
	}

	@Override
	public JdbcValueMapper getJdbcValueMapper() {
		return valueMapper;
	}

	@Override
	public void prepare(
			ResultSetMappingDescriptor.JdbcValuesMetadata jdbcResultsMetadata,
			SessionFactoryImplementor sessionFactory) {
		// resolve the column-alias to a position
		jdbcResultSetPosition = jdbcResultsMetadata.resolveColumnPosition( columnAlias );

		if ( valueMapper == null ) {
			// assume we should auto-discover the type
			final SqlTypeDescriptor sqlTypeDescriptor = jdbcResultsMetadata.resolveSqlTypeDescriptor( jdbcResultSetPosition );

			valueMapper = sqlTypeDescriptor.getJdbcValueMapper(
					sqlTypeDescriptor.getJdbcRecommendedJavaTypeMapping(
							sessionFactory.getTypeConfiguration()
					)
			);
		}

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
