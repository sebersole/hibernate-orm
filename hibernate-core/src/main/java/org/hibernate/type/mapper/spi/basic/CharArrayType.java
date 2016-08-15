/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.spi.java.PrimitiveCharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.VarcharTypeDescriptor;
import org.hibernate.type.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#VARCHAR VARCHAR} and {@code char[]}
 *
 * @author Emmanuel Bernard
 * @author Steve Ebersole
 */
public class CharArrayType
		extends BasicTypeImpl<char[]>
		implements JdbcLiteralFormatter<char[]> {
	public static final CharArrayType INSTANCE = new CharArrayType();

	public CharArrayType() {
		super( PrimitiveCharacterArrayTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "characters"; 
	}

	@Override
	public JdbcLiteralFormatter<char[]> getJdbcLiteralFormatter() {
		return this;
	}

	@Override
	public String toJdbcLiteral(char[] value, Dialect dialect) {
		return StringTypeDescriptor.INSTANCE.toJdbcLiteral( toString( value ), dialect );
	}
}
