/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.type.descriptor.spi.java.basic.StringTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.LongVarcharTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#LONGVARCHAR LONGVARCHAR} and {@link String}
 *
 * @author Gavin King,
 * @author Bertrand Renuart
 * @author Steve Ebersole
 */
public class TextType extends BasicTypeImpl<String> {
	public static final TextType INSTANCE = new TextType();

	public TextType() {
		super( StringTypeDescriptor.INSTANCE, LongVarcharTypeDescriptor.INSTANCE );
	}

	public String getName() { 
		return "text";
	}

	@Override
	public JdbcLiteralFormatter<String> getJdbcLiteralFormatter() {
		// no literal support for long/lob character data
		return null;
	}
}
