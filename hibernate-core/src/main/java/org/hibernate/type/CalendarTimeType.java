/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type;

import java.util.Calendar;

import org.hibernate.type.descriptor.java.CalendarTimeJavaTypeDescriptor;
import org.hibernate.type.descriptor.jdbc.TimeJdbcType;

/**
 * A type mapping {@link java.sql.Types#TIME TIME} and {@link Calendar}.
 * <p/>
 * For example, a Calendar attribute annotated with {@link jakarta.persistence.Temporal} and specifying
 * {@link jakarta.persistence.TemporalType#TIME}
 *
 * @author Steve Ebersole
 */
public class CalendarTimeType
		extends AbstractSingleColumnStandardBasicType<Calendar> {
	public static final CalendarTimeType INSTANCE = new CalendarTimeType();

	public CalendarTimeType() {
		super( TimeJdbcType.INSTANCE, CalendarTimeJavaTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "calendar_time";
	}

}
