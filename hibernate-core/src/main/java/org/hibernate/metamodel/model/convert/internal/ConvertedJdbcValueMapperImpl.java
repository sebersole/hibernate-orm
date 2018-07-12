/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.convert.internal;

import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class ConvertedJdbcValueMapperImpl implements JdbcValueMapper {
	private final BasicJavaDescriptor javaTypeDescriptor;
	private final SqlTypeDescriptor sqlTypeDescriptor;
	private final JdbcValueExtractor convertedValueExtractor;
	private final JdbcValueBinder convertedValueBinder;

	@SuppressWarnings("unchecked")
	public ConvertedJdbcValueMapperImpl(
			BasicValueConverter valueConverter,
			SqlTypeDescriptor sqlTypeDescriptor,
			TypeConfiguration typeConfiguration) {
		this.javaTypeDescriptor = valueConverter.getRelationalJavaDescriptor();
		this.sqlTypeDescriptor = sqlTypeDescriptor;

		final JdbcValueMapper jdbcValueMapper = sqlTypeDescriptor.getJdbcValueMapper( javaTypeDescriptor, typeConfiguration );

		this.convertedValueBinder = new ConvertedJdbcValueBinder(
				sqlTypeDescriptor,
				valueConverter,
				jdbcValueMapper.getJdbcValueBinder()
		);

		this.convertedValueExtractor = new ConvertedJdbcValueExtractor(
				sqlTypeDescriptor,
				valueConverter,
				jdbcValueMapper.getJdbcValueExtractor()
		);
	}

	@Override
	public BasicJavaDescriptor getJavaTypeDescriptor() {
		return javaTypeDescriptor;
	}

	@Override
	public SqlTypeDescriptor getSqlTypeDescriptor() {
		return sqlTypeDescriptor;
	}

	@Override
	public JdbcValueExtractor getJdbcValueExtractor() {
		return convertedValueExtractor;
	}

	@Override
	public JdbcValueBinder getJdbcValueBinder() {
		return convertedValueBinder;
	}
}
