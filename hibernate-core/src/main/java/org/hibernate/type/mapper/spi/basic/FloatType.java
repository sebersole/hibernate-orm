/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.type.descriptor.spi.java.FloatTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * A type that maps between {@link java.sql.Types#FLOAT FLOAT} and {@link Float}
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class FloatType extends BasicTypeImpl<Float> {
	public static final FloatType INSTANCE = new FloatType();

	public static final Float ZERO = 0.0f;

	public FloatType() {
		super( FloatTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.FloatTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return "float";
	}

	@Override
	public JdbcLiteralFormatter<Float> getJdbcLiteralFormatter() {
		return FloatTypeDescriptor.INSTANCE;
	}
}
