/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.internal.CoreLogging;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.spi.JdbcTypeNameMapper;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

import org.jboss.logging.Logger;

/**
 * Convenience base implementation of {@link JdbcValueExtractor}
 *
 * @author Steve Ebersole
 */
public abstract class AbstractJdbcValueExtractor<J> implements JdbcValueExtractor<J> {
	private static final Logger log = CoreLogging.logger( AbstractJdbcValueExtractor.class );

	private final JavaTypeDescriptor<J> javaDescriptor;
	private final SqlTypeDescriptor sqlDescriptor;

	public AbstractJdbcValueExtractor(JavaTypeDescriptor<J> javaDescriptor, SqlTypeDescriptor sqlDescriptor) {
		this.javaDescriptor = javaDescriptor;
		this.sqlDescriptor = sqlDescriptor;
	}

	public JavaTypeDescriptor<J> getJavaDescriptor() {
		return javaDescriptor;
	}

	public SqlTypeDescriptor getSqlDescriptor() {
		return sqlDescriptor;
	}

	@Override
	public J extract(ResultSet rs, SqlSelection selectionMemento, JdbcValuesSourceProcessingState processingState) throws SQLException {
		final J value = doExtract( rs, selectionMemento, processingState );
		final boolean traceEnabled = log.isTraceEnabled();
		if ( value == null || rs.wasNull() ) {
			if ( traceEnabled ) {
				log.tracef(
						"extracted value ([%s] : [%s]) - [null]",
						selectionMemento,
						JdbcTypeNameMapper.getTypeName( getSqlDescriptor().getJdbcTypeCode() )
				);
			}
			return null;
		}
		else {
			if ( traceEnabled ) {
				log.tracef(
						"extracted value ([%s] : [%s]) - [%s]",
						selectionMemento,
						JdbcTypeNameMapper.getTypeName( getSqlDescriptor().getJdbcTypeCode() ),
						getJavaDescriptor().extractLoggableRepresentation( value )
				);
			}
			return value;
		}
	}

	/**
	 * Perform the extraction.
	 *
	 * @implSpec Null checking of the value (including {@link ResultSet#wasNull} checking) is done in caller.
	 */
	protected abstract J doExtract(ResultSet rs, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState) throws SQLException;

	@Override
	public J extract(CallableStatement statement, SqlSelection index, JdbcValuesSourceProcessingState processingState) throws SQLException {
		final J value = doExtract( statement, index, processingState );
		final boolean traceEnabled = log.isTraceEnabled();
		if ( value == null || statement.wasNull() ) {
			if ( traceEnabled ) {
				log.tracef(
						"extracted procedure output  parameter ([%s] : [%s]) - [null]",
						index,
						JdbcTypeNameMapper.getTypeName( getSqlDescriptor().getJdbcTypeCode() )
				);
			}
			return null;
		}
		else {
			if ( traceEnabled ) {
				log.tracef(
						"extracted procedure output  parameter ([%s] : [%s]) - [%s]",
						index,
						JdbcTypeNameMapper.getTypeName( getSqlDescriptor().getJdbcTypeCode() ),
						getJavaDescriptor().extractLoggableRepresentation( value )
				);
			}
			return value;
		}
	}

	/**
	 * Perform the extraction.
	 *
	 * @implSpec Null checking of the value (including {@link ResultSet#wasNull} checking) is done in caller.
	 */
	protected abstract J doExtract(CallableStatement statement, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState) throws SQLException;

	@Override
	public J extract(CallableStatement statement, String name, JdbcValuesSourceProcessingState processingState) throws SQLException {
		final J value = doExtract( statement, name, processingState );
		final boolean traceEnabled = log.isTraceEnabled();
		if ( value == null || statement.wasNull() ) {
			if ( traceEnabled ) {
				log.tracef(
						"extracted named procedure output  parameter ([%s] : [%s]) - [null]",
						name,
						JdbcTypeNameMapper.getTypeName( getSqlDescriptor().getJdbcTypeCode() )
				);
			}
			return null;
		}
		else {
			if ( traceEnabled ) {
				log.tracef(
						"extracted named procedure output  parameter ([%s] : [%s]) - [%s]",
						name,
						JdbcTypeNameMapper.getTypeName( getSqlDescriptor().getJdbcTypeCode() ),
						getJavaDescriptor().extractLoggableRepresentation( value )
				);
			}
			return value;
		}
	}

	/**
	 * Perform the extraction.
	 *
	 * @implSpec Null checking of the value (including {@link ResultSet#wasNull} checking) is done in caller.
	 */
	protected abstract J doExtract(CallableStatement statement, String name, JdbcValuesSourceProcessingState processingState) throws SQLException;
}
