/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java.internal;

import java.util.Collection;

import org.hibernate.type.descriptor.java.spi.AbstractBasicJavaDescriptor;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.spi.WrapperOptions;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class CollectionJavaDescriptor<T> extends AbstractBasicJavaDescriptor<T> {
		protected CollectionJavaDescriptor(Class<T> type) {
		super( type );
	}

	@Override
	public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public String toString(T value) {
		return "{java.util.Collection}";
	}


	@Override
	public String getTypeName() {
		return getJavaType().getName();
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		// none
		return null;
	}

	@Override
	public <X> T wrap(X value, WrapperOptions options) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public T fromString(String value) {
		throw new UnsupportedOperationException(  );
	}
}
