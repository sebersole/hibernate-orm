/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.type.descriptor.spi.java.basic.CharacterTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.NCharTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#NCHAR NCHAR(1)} and {@link Character}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class CharacterNCharType
		extends BasicTypeImpl<Character> {

	public static final CharacterNCharType INSTANCE = new CharacterNCharType();

	public CharacterNCharType() {
		super( CharacterTypeDescriptor.INSTANCE, NCharTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "ncharacter";
	}

	@Override
	public JdbcLiteralFormatter<Character> getJdbcLiteralFormatter() {
		return NCharTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( CharacterTypeDescriptor.INSTANCE );
	}
}
