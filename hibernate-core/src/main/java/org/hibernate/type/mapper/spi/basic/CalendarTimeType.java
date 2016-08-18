/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.util.Calendar;

import org.hibernate.type.descriptor.spi.java.basic.CalendarTimeTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.TimeTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * A type mapping {@link java.sql.Types#TIME TIME} and {@link Calendar}.
 * <p/>
 * For example, a Calendar attribute annotated with {@link javax.persistence.Temporal} and specifying
 * {@link javax.persistence.TemporalType#TIME}
 *
 * @author Steve Ebersole
 */
public class CalendarTimeType
		extends TemporalTypeImpl<Calendar> {
	public static final CalendarTimeType INSTANCE = new CalendarTimeType();

	public CalendarTimeType() {
		super( CalendarTimeTypeDescriptor.INSTANCE, TimeTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "calendar_time";
	}

	@Override
	public JdbcLiteralFormatter<Calendar> getJdbcLiteralFormatter() {
		return TimeTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( CalendarTimeTypeDescriptor.INSTANCE );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> TemporalType<X> resolveTypeForPrecision(javax.persistence.TemporalType precision, TypeConfiguration typeConfiguration) {
		switch ( precision ) {
			case TIMESTAMP: {
				return (TemporalType<X>) CalendarType.INSTANCE;
			}
			case DATE: {
				return (TemporalType<X>) CalendarDateType.INSTANCE;
			}
			default: {
				return (TemporalType<X>) this;
			}
		}
	}
}
