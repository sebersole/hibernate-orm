/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.time.LocalTime;

import org.hibernate.type.descriptor.spi.java.LocalTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.sql.TimeTypeDescriptor;
import org.hibernate.type.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#TIMESTAMP TIMESTAMP} and {@link java.time.LocalDateTime}.
 *
 * @author Steve Ebersole
 */
public class LocalTimeType extends TemporalTypeImpl<LocalTime> {
	/**
	 * Singleton access
	 */
	public static final LocalTimeType INSTANCE = new LocalTimeType();

	/**
	 * NOTE: protected access to allow for sub-classing
	 */
	@SuppressWarnings("WeakerAccess")
	protected LocalTimeType() {
		super( LocalTimeJavaDescriptor.INSTANCE, TimeTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return LocalTime.class.getSimpleName();
	}

	@Override
	public JdbcLiteralFormatter<LocalTime> getJdbcLiteralFormatter() {
		return LocalTimeJavaDescriptor.INSTANCE.getJdbcLiteralFormatter();
	}
}
