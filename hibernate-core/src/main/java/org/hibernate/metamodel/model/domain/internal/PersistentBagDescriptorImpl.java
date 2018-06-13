/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.Collection;
import java.util.Set;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractPersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.type.descriptor.java.internal.CollectionJavaDescriptor;

/**
 * @author Steve Ebersole
 */
public class PersistentBagDescriptorImpl<O,E> extends AbstractPersistentCollectionDescriptor<O,Collection<E>,E> {
	public PersistentBagDescriptorImpl(
			Property bootProperty,
			ManagedTypeDescriptor runtimeContainer,
			RuntimeModelCreationContext context) {
		super( bootProperty, runtimeContainer, context );
	}

	@Override
	protected CollectionJavaDescriptor resolveCollectionJtd(
			org.hibernate.mapping.Collection collectionBinding,
			RuntimeModelCreationContext creationContext) {
		return (CollectionJavaDescriptor) creationContext.getTypeConfiguration()
				.getJavaTypeDescriptorRegistry()
				.getDescriptor( Set.class );
	}

	@Override
	public PersistentCollection instantiateWrapper(SharedSessionContractImplementor session, Object key) {
		return new PersistentBag( session, this, key );
	}

	@Override
	@SuppressWarnings("unchecked")
	public PersistentCollection wrap(SharedSessionContractImplementor session, Collection<E> rawCollection) {
		return new PersistentBag( session, this, rawCollection );
	}



	@Override
	public boolean contains(Object collection, Object childObject) {
		return ( (Collection ) collection ).contains( childObject );
	}
}
