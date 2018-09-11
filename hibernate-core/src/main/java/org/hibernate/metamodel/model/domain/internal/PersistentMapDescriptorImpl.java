/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.Comparator;
import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractPersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.results.internal.domain.collection.CollectionInitializerProducer;
import org.hibernate.sql.results.internal.domain.collection.MapInitializerProducer;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.type.descriptor.java.internal.CollectionJavaDescriptor;

/**
 * @author Steve Ebersole
 */
public class PersistentMapDescriptorImpl<O,K,E>
		extends AbstractPersistentCollectionDescriptor<O,Map<K,E>,E> {
	private final Comparator<K> comparator;

	@SuppressWarnings("unchecked")
	public PersistentMapDescriptorImpl(
			Property pluralProperty,
			ManagedTypeDescriptor runtimeContainer,
			RuntimeModelCreationContext creationContext) throws MappingException, CacheException {
		super( pluralProperty, runtimeContainer, creationContext );

		if ( pluralProperty.getValue() instanceof Collection ) {
			this.comparator = ( (Collection) pluralProperty.getValue() ).getComparator();
		}
		else {
			this.comparator = null;
		}
	}

	@Override
	protected CollectionJavaDescriptor resolveCollectionJtd(
			Collection collectionBinding,
			RuntimeModelCreationContext creationContext) {
		return findCollectionJtd( Map.class, creationContext );
	}

	@Override
	public Comparator<?> getSortingComparator() {
		return comparator;
	}


	@Override
	public boolean contains(Object collection, Object childObject) {
		// todo (6.0) : do we need to check key as well?
		// todo (6.0) : or perhaps make distinction between #containsValue and #containsKey/Index?
		return ( (Map) collection ).containsValue( childObject );
	}

	@Override
	protected CollectionInitializerProducer createInitializerProducer(
			FetchParent fetchParent,
			boolean isJoinFetch,
			String resultVariable,
			LockMode lockMode,
			DomainResult keyResult,
			DomainResultCreationState creationState,
			DomainResultCreationContext creationContext) {
		final NavigableReference navigableReference = creationState.getNavigableReferenceStack().getCurrent();

		final DomainResult mapKeyResult = getIndexDescriptor().createDomainResult(
				navigableReference,
				null,
				creationContext,
				creationState
		);

		final DomainResult mapValueResult = getElementDescriptor().createDomainResult(
				navigableReference,
				resultVariable,
				creationContext,
				creationState
		);

		return new MapInitializerProducer(
				this,
				isJoinFetch,
				mapKeyResult,
				mapValueResult
		);
	}

}
