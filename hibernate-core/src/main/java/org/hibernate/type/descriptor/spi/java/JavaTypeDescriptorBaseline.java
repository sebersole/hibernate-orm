/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.spi.java;

import org.hibernate.type.descriptor.spi.java.basic.BigDecimalTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.BigIntegerTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.BlobTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.BooleanTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ByteTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CalendarTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CharacterTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ClassTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ClobTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CurrencyTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.DateTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.DoubleTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.DurationJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.FloatTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.InstantJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.IntegerTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.JavaTypeDescriptorBasicImplementor;
import org.hibernate.type.descriptor.spi.java.basic.JdbcDateTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.JdbcTimeTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.JdbcTimestampTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LocalDateJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LocalDateTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LocaleTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LongTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.NClobTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.OffsetDateTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.OffsetTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.PrimitiveCharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ShortTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.StringTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.TimeZoneTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.UrlTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ZonedDateTimeJavaDescriptor;

/**
 *
 * @author Steve Ebersole
 */
public class JavaTypeDescriptorBaseline {
	interface BaselineTarget {
		void addBaselineDescriptor(JavaTypeDescriptorBasicImplementor descriptor);
		void addBaselineDescriptor(Class describedJavaType, JavaTypeDescriptorBasicImplementor descriptor);
	}

	public static void prime(BaselineTarget target) {
		target.addBaselineDescriptor( ByteTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( BooleanTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( CharacterTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( ShortTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( IntegerTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( LongTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( FloatTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( DoubleTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( BigDecimalTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( BigIntegerTypeDescriptor.INSTANCE );

		target.addBaselineDescriptor( StringTypeDescriptor.INSTANCE );

		target.addBaselineDescriptor( BlobTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( ClobTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( NClobTypeDescriptor.INSTANCE );

		target.addBaselineDescriptor( ByteArrayTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( CharacterArrayTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( PrimitiveByteArrayTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( PrimitiveCharacterArrayTypeDescriptor.INSTANCE );

		target.addBaselineDescriptor( DurationJavaDescriptor.INSTANCE );
		target.addBaselineDescriptor( InstantJavaDescriptor.INSTANCE );
		target.addBaselineDescriptor( LocalDateJavaDescriptor.INSTANCE );
		target.addBaselineDescriptor( LocalDateTimeJavaDescriptor.INSTANCE );
		target.addBaselineDescriptor( OffsetDateTimeJavaDescriptor.INSTANCE );
		target.addBaselineDescriptor( OffsetTimeJavaDescriptor.INSTANCE );
		target.addBaselineDescriptor( ZonedDateTimeJavaDescriptor.INSTANCE );

		target.addBaselineDescriptor( CalendarTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( DateTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( java.sql.Date.class, JdbcDateTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( java.sql.Time.class, JdbcTimeTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( java.sql.Timestamp.class, JdbcTimestampTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( TimeZoneTypeDescriptor.INSTANCE );

		target.addBaselineDescriptor( ClassTypeDescriptor.INSTANCE );

		target.addBaselineDescriptor( CurrencyTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( LocaleTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( UrlTypeDescriptor.INSTANCE );
		target.addBaselineDescriptor( UUIDTypeDescriptor.INSTANCE );

	}
}
