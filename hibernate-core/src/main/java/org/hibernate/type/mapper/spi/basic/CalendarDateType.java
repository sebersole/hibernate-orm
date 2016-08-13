/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.util.Calendar;

import org.hibernate.type.descriptor.spi.java.CalendarDateTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.DateTypeDescriptor;
import org.hibernate.type.spi.JdbcLiteralFormatter;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * A type mapping {@link java.sql.Types#DATE DATE} and {@link Calendar}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class CalendarDateType
		extends TemporalTypeImpl<Calendar>
		implements TemporalType<Calendar> {
	public static final CalendarDateType INSTANCE = new CalendarDateType();

	public CalendarDateType() {
		super( CalendarDateTypeDescriptor.INSTANCE, DateTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "calendar_date";
	}

	@Override
	public JdbcLiteralFormatter<Calendar> getJdbcLiteralFormatter() {
		return CalendarDateTypeDescriptor.INSTANCE;
	}

	@Override
	public javax.persistence.TemporalType getPrecision() {
		return javax.persistence.TemporalType.DATE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> TemporalType<X> resolveTypeForPrecision(javax.persistence.TemporalType precision, TypeConfiguration typeConfiguration) {
		switch ( precision ) {
			case TIMESTAMP: {
				return (TemporalType<X>) CalendarTimeType.INSTANCE;
			}
			case TIME: {
				return (TemporalType<X>) CalendarType.INSTANCE;
			}
			default: {
				return (TemporalType<X>) this;
			}
		}
	}
}
