/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.type.descriptor.spi.java.DoubleTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#DOUBLE DOUBLE} and {@link Double}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class DoubleType extends BasicTypeImpl<Double> {
	public static final DoubleType INSTANCE = new DoubleType();

	public static final Double ZERO = 0.0;

	public DoubleType() {
		super( DoubleTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.DoubleTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return "double";
	}

	@Override
	public JdbcLiteralFormatter<Double> getJdbcLiteralFormatter() {
		return DoubleTypeDescriptor.INSTANCE;
	}
}
