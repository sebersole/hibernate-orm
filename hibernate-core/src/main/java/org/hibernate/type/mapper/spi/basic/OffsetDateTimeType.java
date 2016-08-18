/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.time.OffsetDateTime;
import java.util.Comparator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.spi.java.basic.OffsetDateTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.sql.TimestampTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * @author Steve Ebersole
 */
public class OffsetDateTimeType
		extends TemporalTypeImpl<OffsetDateTime>
		implements VersionSupport<OffsetDateTime> {

	/**
	 * Singleton access
	 */
	public static final OffsetDateTimeType INSTANCE = new OffsetDateTimeType();

	/**
	 * NOTE: protected access to allow for sub-classing
	 */
	@SuppressWarnings("WeakerAccess")
	protected OffsetDateTimeType() {
		super( OffsetDateTimeJavaDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return OffsetDateTime.class.getSimpleName();
	}

	@Override
	public VersionSupport<OffsetDateTime> getVersionSupport() {
		return this;
	}

	@Override
	public OffsetDateTime seed(SharedSessionContractImplementor session) {
		return OffsetDateTime.now();
	}

	@Override
	public OffsetDateTime next(OffsetDateTime current, SharedSessionContractImplementor session) {
		return OffsetDateTime.now();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Comparator<OffsetDateTime> getComparator() {
		return OffsetDateTime.timeLineOrder();
	}

	@Override
	public JdbcLiteralFormatter<OffsetDateTime> getJdbcLiteralFormatter() {
		return TimestampTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( OffsetDateTimeJavaDescriptor.INSTANCE );
	}

}
