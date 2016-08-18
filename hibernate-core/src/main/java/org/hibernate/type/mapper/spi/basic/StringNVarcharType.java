/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.type.descriptor.spi.java.basic.StringTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.NVarcharTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#VARCHAR VARCHAR} and {@link String}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class StringNVarcharType extends BasicTypeImpl<String> {

	public static final StringNVarcharType INSTANCE = new StringNVarcharType();

	public StringNVarcharType() {
		super( StringTypeDescriptor.INSTANCE, NVarcharTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "nstring";
	}

	@Override
	public JdbcLiteralFormatter<String> getJdbcLiteralFormatter() {
		return NVarcharTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( StringTypeDescriptor.INSTANCE );
	}

	@Override
	public String toString(String value) {
		return value;
	}
}
