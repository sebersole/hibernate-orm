/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java.internal;

import org.hibernate.collection.spi.PersistentCollectionTuplizer;
import org.hibernate.type.descriptor.java.spi.AbstractBasicJavaDescriptor;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.spi.WrapperOptions;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractCollectionJavaDescriptor<T> extends AbstractBasicJavaDescriptor<T> {
	private final PersistentCollectionTuplizer tuplizer;

	public AbstractCollectionJavaDescriptor(Class<T> type, PersistentCollectionTuplizer tuplizer) {
		super( type );
		this.tuplizer = tuplizer;
	}

	@Override
	public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public T fromString(String value) {
		throw new UnsupportedOperationException();
	}

	public Object indexOf(Object collection, Object element) {
		throw new UnsupportedOperationException( "generic collections don't have indexes" );
	}

	public PersistentCollectionTuplizer getTuplizer() {
		return tuplizer;
	}
}
