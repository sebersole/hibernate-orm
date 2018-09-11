/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.spi;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.sql.exec.spi.ExecutionContext;

/**
 * Represents a collection currently being loaded.
 *
 * @author Steve Ebersole
 */
public class LoadingCollectionEntry {
	private final PersistentCollectionDescriptor collectionDescriptor;
	private final CollectionInitializer initializer;
	private final Object key;
	private final PersistentCollection collectionInstance;

	public LoadingCollectionEntry(
			PersistentCollectionDescriptor collectionDescriptor,
			CollectionInitializer initializer,
			Object key,
			PersistentCollection collectionInstance) {
		this.collectionDescriptor = collectionDescriptor;
		this.initializer = initializer;
		this.key = key;
		this.collectionInstance = collectionInstance;
	}

	public PersistentCollectionDescriptor getCollectionDescriptor() {
		return collectionDescriptor;
	}

	/**
	 * Access to the initializer that is responsible for initializing this collection
	 */
	public CollectionInitializer getInitializer() {
		return initializer;
	}

	public Object getKey() {
		return key;
	}

	public PersistentCollection getCollectionInstance() {
		return collectionInstance;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + getCollectionDescriptor().getNavigableRole().getFullPath() + "#" + getKey() + ")";
	}

	public void finishLoading(ExecutionContext executionContext) {
		collectionInstance.endRead();
		collectionInstance.afterInitialize();
	}
}
