/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.util.TimeZone;

import org.hibernate.type.descriptor.spi.java.TimeZoneTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.VarcharTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type mapping {@link java.sql.Types#VARCHAR VARCHAR} and {@link TimeZone}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class TimeZoneType
		extends BasicTypeImpl<TimeZone> {

	public static final TimeZoneType INSTANCE = new TimeZoneType();

	public TimeZoneType() {
		super( TimeZoneTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "timezone";
	}

	@Override
	public JdbcLiteralFormatter<TimeZone> getJdbcLiteralFormatter() {
		return VarcharTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( TimeZoneTypeDescriptor.INSTANCE );
	}
}
