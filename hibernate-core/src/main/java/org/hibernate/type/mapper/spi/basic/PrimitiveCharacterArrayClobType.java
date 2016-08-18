/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;
import org.hibernate.type.descriptor.spi.java.basic.PrimitiveCharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.ClobTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * Map a char[] to a Clob
 *
 * @author Emmanuel Bernard
 */
public class PrimitiveCharacterArrayClobType extends BasicTypeImpl<char[]> {
	public static final CharacterArrayClobType INSTANCE = new CharacterArrayClobType();

	public PrimitiveCharacterArrayClobType() {
		super( PrimitiveCharacterArrayTypeDescriptor.INSTANCE, ClobTypeDescriptor.DEFAULT );
	}

	public String getName() {
		// todo name these annotation types for addition to the registry
		return null;
	}

	@Override
	public JdbcLiteralFormatter<char[]> getJdbcLiteralFormatter() {
		// no literal support for CLOB
		return null;
	}
}
