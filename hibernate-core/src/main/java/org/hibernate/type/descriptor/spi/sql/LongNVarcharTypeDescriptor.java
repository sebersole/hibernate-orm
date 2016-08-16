/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.spi.sql;

import java.sql.Types;

import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptor;
import org.hibernate.type.mapper.spi.JdbcLiteralFormatter;

/**
 * Descriptor for {@link Types#LONGNVARCHAR LONGNVARCHAR} handling.
 *
 * @author Steve Ebersole
 */
public class LongNVarcharTypeDescriptor extends NVarcharTypeDescriptor {
	public static final LongNVarcharTypeDescriptor INSTANCE = new LongNVarcharTypeDescriptor();

	public LongNVarcharTypeDescriptor() {
	}

	@Override
	public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
		// no literal support for long character data
		return null;
	}

	@Override
	public int getSqlType() {
		return Types.LONGNVARCHAR;
	}
}
