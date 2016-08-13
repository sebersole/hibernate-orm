/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.spi;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.mapper.spi.basic.AttributeConverterDefinition;
import org.hibernate.type.spi.JdbcLiteralFormatter;

/**
 * @author Steve Ebersole
 */
public class JdbcLiteralFormatterConvertedImpl<T> implements JdbcLiteralFormatter<T> {
	private final AttributeConverterDefinition<T,Object> attributeConverterDefinition;

	public JdbcLiteralFormatterConvertedImpl(AttributeConverterDefinition<T, Object> attributeConverterDefinition) {
		this.attributeConverterDefinition = attributeConverterDefinition;
	}

	@Override
	public String toJdbcLiteral(T value, Dialect dialect) {
		return attributeConverterDefinition.getJdbcType().getJdbcLiteralFormatter().toJdbcLiteral(
				attributeConverterDefinition.getAttributeConverter().convertToDatabaseColumn( value ),
				dialect
		);
	}
}
