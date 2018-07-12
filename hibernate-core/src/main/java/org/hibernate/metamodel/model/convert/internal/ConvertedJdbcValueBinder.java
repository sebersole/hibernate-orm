/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.convert.internal;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.sql.AbstractJdbcValueBinder;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class ConvertedJdbcValueBinder extends AbstractJdbcValueBinder {
	private final BasicValueConverter valueConverter;
	private final JdbcValueBinder physicalBinder;

	public ConvertedJdbcValueBinder(
			SqlTypeDescriptor sqlDescriptor,
			BasicValueConverter valueConverter,
			JdbcValueBinder physicalBinder) {
		super( valueConverter.getRelationalJavaDescriptor(), sqlDescriptor );
		this.valueConverter = valueConverter;
		this.physicalBinder = physicalBinder;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void doBind(
			PreparedStatement st,
			int index, Object value,
			ExecutionContext executionContext) throws SQLException {
		physicalBinder.bind(
				st,
				index, convert( value, executionContext ),
				executionContext
		);
	}

	@SuppressWarnings("unchecked")
	private Object convert(Object domainValue, ExecutionContext executionContext) {
		return valueConverter.toRelationalValue( domainValue, executionContext.getSession() );
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void doBind(
			CallableStatement st,
			String name, Object value,
			ExecutionContext executionContext) throws SQLException {
		physicalBinder.bind(
				st,
				name, convert( value, executionContext ),
				executionContext
		);
	}
}
