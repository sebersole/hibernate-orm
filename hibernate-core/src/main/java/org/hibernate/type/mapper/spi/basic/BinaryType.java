/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.util.Comparator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.spi.java.basic.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.VarbinaryTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;;

/**
 * A type that maps between a {@link java.sql.Types#VARBINARY VARBINARY} and {@code byte[]}
 *
 * Implementation of the {@link VersionType} interface should be considered deprecated.
 * For binary entity versions/timestamps, {@link RowVersionType} should be used instead.
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class BinaryType extends BasicTypeImpl<byte[]> implements VersionSupport<byte[]> {
	public static final BinaryType INSTANCE = new BinaryType();

	public String getName() {
		return "binary";
	}

	public BinaryType() {
		super( PrimitiveByteArrayTypeDescriptor.INSTANCE, VarbinaryTypeDescriptor.INSTANCE );
	}

	@Override
	public VersionSupport<byte[]> getVersionSupport() {
		return this;
	}

	@Override
	public byte[] seed(SharedSessionContractImplementor session) {
		// Note : simply returns null for seed() and next() as the only known
		// 		application of binary types for versioning is for use with the
		// 		TIMESTAMP datatype supported by Sybase and SQL Server, which
		// 		are completely db-generated values...
		return null;
	}

	/**
	 * Increment the version.
	 *
	 * @param session The session from which this request originates.
	 * @param current the current version
	 * @return an instance of the type
	 * @deprecated use {@link RowVersionType} for binary entity versions/timestamps
	 */
	@Override
	@Deprecated
	public byte[] next(byte[] current, SharedSessionContractImplementor session) {
		return current;
	}

	/**
	 * Get a comparator for version values.
	 *
	 * @return The comparator to use to compare different version values.
	 * @deprecated use {@link RowVersionType} for binary entity versions/timestamps
	 */
	@Override
	@Deprecated
	public Comparator<byte[]> getComparator() {
		return PrimitiveByteArrayTypeDescriptor.INSTANCE.getComparator();
	}

	@Override
	public JdbcLiteralFormatter<byte[]> getJdbcLiteralFormatter() {
		// no support for binary literals
		return null;
	}
}
