/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.spi.java.IntegerTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#INTEGER INTEGER} and @link Integer}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class IntegerType
		extends BasicTypeImpl<Integer>
		implements VersionSupport<Integer> {

	public static final IntegerType INSTANCE = new IntegerType();

	public static final Integer ZERO = 0;

	public IntegerType() {
		super( IntegerTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.IntegerTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return "integer";
	}

	@Override
	public VersionSupport<Integer> getVersionSupport() {
		return this;
	}

	@Override
	public Integer seed(SharedSessionContractImplementor session) {
		return ZERO;
	}

	@Override
	public Integer next(Integer current, SharedSessionContractImplementor session) {
		return current + 1;
	}

	@Override
	public JdbcLiteralFormatter<Integer> getJdbcLiteralFormatter() {
		return org.hibernate.type.descriptor.spi.sql.IntegerTypeDescriptor.INSTANCE.getJdbcLiteralFormatter(
				IntegerTypeDescriptor.INSTANCE
		);
	}
}
