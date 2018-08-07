/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.List;

import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractPersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.type.descriptor.java.internal.CollectionJavaDescriptor;

/**
 * Hibernate's standard PersistentCollectionDescriptor implementor
 * for Lists
 *
 * @author Steve Ebersole
 */
public class PersistentListDescriptorImpl<O,E> extends AbstractPersistentCollectionDescriptor<O,List<E>, E> {
	public PersistentListDescriptorImpl(
			Property bootProperty,
			ManagedTypeDescriptor runtimeContainer,
			RuntimeModelCreationContext context) {
		super( bootProperty, runtimeContainer, context );
	}

	@Override
	protected CollectionJavaDescriptor resolveCollectionJtd(
			Collection collectionBinding,
			RuntimeModelCreationContext creationContext) {
		return findCollectionJtd( List.class, creationContext );
	}

	@Override
	public PersistentCollection instantiateWrapper(SharedSessionContractImplementor session, Object key) {
		return new PersistentList( session, this, key );
	}

	@Override
	@SuppressWarnings("unchecked")
	public PersistentCollection<E> wrap(SharedSessionContractImplementor session, List<E> rawCollection) {
		return new PersistentList( session, this, rawCollection );
	}

	@Override
	public boolean contains(Object collection, Object childObject) {
		return ( (List) collection ).contains( childObject );
	}


}
