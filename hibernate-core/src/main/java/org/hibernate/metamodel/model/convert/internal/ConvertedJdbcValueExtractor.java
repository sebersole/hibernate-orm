/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.convert.internal;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.sql.AbstractJdbcValueExtractor;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class ConvertedJdbcValueExtractor extends AbstractJdbcValueExtractor {
	private final BasicValueConverter valueConverter;
	private final JdbcValueExtractor physicalExtractor;

	@SuppressWarnings({"WeakerAccess", "unchecked"})
	public ConvertedJdbcValueExtractor(
			SqlTypeDescriptor sqlDescriptor,
			BasicValueConverter valueConverter,
			JdbcValueExtractor physicalExtractor) {
		super( valueConverter.getRelationalJavaDescriptor(), sqlDescriptor );
		this.valueConverter = valueConverter;
		this.physicalExtractor = physicalExtractor;
	}

	@Override
	protected Object doExtract(
			ResultSet rs,
			int position,
			ExecutionContext executionContext) throws SQLException {
		return convert(
				physicalExtractor.extract(
						rs,
						position,
						executionContext
				),
				executionContext
		);
	}

	@SuppressWarnings("unchecked")
	private Object convert(Object value, ExecutionContext executionContext) {
		return valueConverter.toDomainValue( value, executionContext.getSession() );
	}

	@Override
	protected Object doExtract(
			CallableStatement statement,
			int position,
			ExecutionContext executionContext) throws SQLException {
		return convert(
				physicalExtractor.extract(
						statement,
						position,
						executionContext
				),
				executionContext
		);
	}

	@Override
	protected Object doExtract(
			CallableStatement statement,
			String name,
			ExecutionContext executionContext) throws SQLException {
		return convert(
				physicalExtractor.extract(
						statement,
						name,
						executionContext
				),
				executionContext
		);
	}
}
