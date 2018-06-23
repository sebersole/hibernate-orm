/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.lang.reflect.Array;
import java.util.Map;

import org.hibernate.collection.internal.PersistentArrayHolder;
import org.hibernate.collection.internal.StandardArraySemantics;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractPersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.type.descriptor.java.internal.CollectionJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class PersistentArrayDescriptorImpl<O,E> extends AbstractPersistentCollectionDescriptor<O,E[], E> {

	public PersistentArrayDescriptorImpl(
			Property pluralProperty,
			ManagedTypeDescriptor runtimeContainer,
			RuntimeModelCreationContext creationContext) {
		super( pluralProperty, runtimeContainer, creationContext );
	}

	@Override
	@SuppressWarnings("unchecked")
	protected CollectionJavaDescriptor resolveCollectionJtd(
			Collection collectionBinding,
			RuntimeModelCreationContext creationContext) {
		Class componentType = getElementDescriptor().getJavaTypeDescriptor().getJavaType();
		if ( componentType == null ) {
			// MAP entity mode?
			// todo (6.0) : verify this
			componentType = Map.class;
		}

		// The only way I know to handle this is by instantiating an array...
		// todo (6.0) : better way?
		final Class arrayType = Array.newInstance( componentType, 0 ).getClass();
		assert arrayType.isArray();

		final CollectionJavaDescriptor javaDescriptor = new CollectionJavaDescriptor(
				arrayType,
				StandardArraySemantics.INSTANCE
		);
		creationContext.getTypeConfiguration().getJavaTypeDescriptorRegistry().addDescriptor( javaDescriptor );

		return javaDescriptor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public E[] instantiateRaw(int anticipatedSize) {
		return (E[]) Array.newInstance(
				getJavaTypeDescriptor().getJavaType().getComponentType(),
				anticipatedSize
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public PersistentCollection instantiateWrapper(SharedSessionContractImplementor session, Object key) {
		return new PersistentArrayHolder( session, this, key );
	}

	@Override
	@SuppressWarnings("unchecked")
	public PersistentCollection wrap(SharedSessionContractImplementor session, E[] rawCollection) {
		return new PersistentArrayHolder( session, this, rawCollection );
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean contains(Object collection, Object childObject) {
		assert collection.getClass().isArray();

		final int length = Array.getLength( collection );
		for ( int i = 0; i < length; i++ ) {
			final JavaTypeDescriptor javaTypeDescriptor = getElementDescriptor().getJavaTypeDescriptor();
			if ( javaTypeDescriptor.areEqual( Array.get( collection, i ), childObject ) ) {
				return true;
			}
		}

		return false;
	}

}
