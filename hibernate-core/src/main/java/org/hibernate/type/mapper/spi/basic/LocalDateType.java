/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.time.LocalDate;

import org.hibernate.type.descriptor.spi.java.basic.LocalDateJavaDescriptor;
import org.hibernate.type.descriptor.spi.sql.DateTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class LocalDateType extends TemporalTypeImpl<LocalDate> {

	/**
	 * Singleton access
	 */
	public static final LocalDateType INSTANCE = new LocalDateType();

	/**
	 * NOTE: protected access to allow for sub-classing
	 */
	@SuppressWarnings("WeakerAccess")
	protected LocalDateType() {
		super( LocalDateJavaDescriptor.INSTANCE, DateTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return LocalDate.class.getSimpleName();
	}

	@Override
	public JdbcLiteralFormatter<LocalDate> getJdbcLiteralFormatter() {
		return DateTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( LocalDateJavaDescriptor.INSTANCE );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> TemporalType<X> resolveTypeForPrecision(
			javax.persistence.TemporalType precision,
			TypeConfiguration typeConfiguration) {
		switch ( precision ) {
			case TIME: {
				return (TemporalType<X>) LocalTimeType.INSTANCE;
			}
			case TIMESTAMP: {
				return (TemporalType<X>) LocalDateTimeType.INSTANCE;
			}
			default: {
				return (TemporalType<X>) this;
			}
		}
	}
}
