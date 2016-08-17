/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.time.OffsetTime;

import org.hibernate.type.descriptor.spi.java.OffsetTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.sql.TimeTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * @author Steve Ebersole
 */
public class OffsetTimeType extends TemporalTypeImpl<OffsetTime> {

	/**
	 * Singleton access
	 */
	public static final OffsetTimeType INSTANCE = new OffsetTimeType();

	/**
	 * NOTE: protected access to allow for sub-classing
	 */
	@SuppressWarnings("WeakerAccess")
	protected OffsetTimeType() {
		super( OffsetTimeJavaDescriptor.INSTANCE, TimeTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return OffsetTime.class.getSimpleName();
	}

	@Override
	public JdbcLiteralFormatter<OffsetTime> getJdbcLiteralFormatter() {
		return TimeTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( OffsetTimeJavaDescriptor.INSTANCE );
	}
}
