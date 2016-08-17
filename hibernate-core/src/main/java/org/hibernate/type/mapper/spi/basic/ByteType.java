/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.util.Comparator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.spi.java.ByteTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.TinyIntTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#TINYINT TINYINT} and {@link Byte}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class ByteType
		extends BasicTypeImpl<Byte>
		implements VersionSupport<Byte> {

	public static final ByteType INSTANCE = new ByteType();

	private static final Byte ZERO = (byte) 0;

	protected ByteType() {
		super( ByteTypeDescriptor.INSTANCE, TinyIntTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return "byte";
	}

	@Override
	public JdbcLiteralFormatter<Byte> getJdbcLiteralFormatter() {
		return TinyIntTypeDescriptor.INSTANCE.getJdbcLiteralFormatter( ByteTypeDescriptor.INSTANCE );
	}

	@Override
	public VersionSupport<Byte> getVersionSupport() {
		return this;
	}

	@Override
	public Byte next(Byte current, SharedSessionContractImplementor session) {
		return (byte) ( current + 1 );
	}

	@Override
	public Byte seed(SharedSessionContractImplementor session) {
		return ZERO;
	}

	@Override
	public Comparator<Byte> getComparator() {
		return getJavaTypeDescriptor().getComparator();
	}
}
