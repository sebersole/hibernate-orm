/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type;

import org.hibernate.type.spi.JdbcLiteralFormatter;
import org.hibernate.type.spi.basic.BasicTypeImpl;
import org.hibernate.type.descriptor.java.internal.PrimitiveCharacterArrayJavaDescriptor;
import org.hibernate.type.spi.descriptor.sql.NClobTypeDescriptor;

/**
 * Map a char[] to a NClob
 *
 * @author Emmanuel Bernard
 */
public class PrimitiveCharacterArrayNClobType extends BasicTypeImpl<char[]> {
	public static final CharacterArrayClobType INSTANCE = new CharacterArrayClobType();

	public PrimitiveCharacterArrayNClobType() {
		super( PrimitiveCharacterArrayJavaDescriptor.INSTANCE, NClobTypeDescriptor.DEFAULT );
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
