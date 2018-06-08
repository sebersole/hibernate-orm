/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.spi;

import java.io.Serializable;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;

/**
 * Represents a collection currently being loaded.
 *
 * @author Steve Ebersole
 */
public class LoadingCollectionEntry {
	private final PersistentCollectionDescriptor collectionDescriptor;
	private final Serializable key;
	private final PersistentCollection collectionInstance;

	LoadingCollectionEntry(
			PersistentCollectionDescriptor collectionDescriptor,
			Serializable key,
			PersistentCollection collectionInstance) {
		this.collectionDescriptor = collectionDescriptor;
		this.key = key;
		this.collectionInstance = collectionInstance;
	}

	public PersistentCollectionDescriptor getCollectionDescriptor() {
		return collectionDescriptor;
	}

	public Serializable getKey() {
		return key;
	}

	public PersistentCollection getCollectionInstance() {
		return collectionInstance;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + getCollectionDescriptor().getNavigableRole().getFullPath() + "#" + getKey() + ")";
	}
}
