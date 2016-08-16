/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.spi.java;

import java.util.Comparator;
import java.util.TimeZone;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.internal.java.ComparatorTimeZoneImpl;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.spi.WrapperOptions;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * Descriptor for {@link TimeZone} handling.
 *
 * @author Steve Ebersole
 */
public class TimeZoneTypeDescriptor
		extends AbstractTypeDescriptorBasicImpl<TimeZone> {
	public static final TimeZoneTypeDescriptor INSTANCE = new TimeZoneTypeDescriptor();

	public TimeZoneTypeDescriptor() {
		super( TimeZone.class );
	}

	public String toString(TimeZone value) {
		return value.getID();
	}

	public TimeZone fromString(String string) {
		return TimeZone.getTimeZone( string );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		return StringTypeDescriptor.INSTANCE.getJdbcRecommendedSqlType( context );
	}

	@Override
	public Comparator<TimeZone> getComparator() {
		return ComparatorTimeZoneImpl.INSTANCE;
	}

	@SuppressWarnings({ "unchecked" })
	public <X> X unwrap(TimeZone value, Class<X> type, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( String.class.isAssignableFrom( type ) ) {
			return (X) toString( value );
		}
		throw unknownUnwrap( type );
	}

	public <X> TimeZone wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( String.class.isInstance( value ) ) {
			return fromString( (String) value );
		}
		throw unknownWrap( value.getClass() );
	}
}
