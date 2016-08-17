/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.util.Date;

import org.hibernate.type.descriptor.spi.java.JdbcDateTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.DateTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#DATE DATE} and {@link java.sql.Date}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class DateType
		extends TemporalTypeImpl<Date> {

	public static final DateType INSTANCE = new DateType();

	protected DateType() {
		super( JdbcDateTypeDescriptor.INSTANCE, DateTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return "date";
	}

	@Override
	public JdbcLiteralFormatter<Date> getJdbcLiteralFormatter() {
		return DateTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( JdbcDateTypeDescriptor.INSTANCE );
	}
}
