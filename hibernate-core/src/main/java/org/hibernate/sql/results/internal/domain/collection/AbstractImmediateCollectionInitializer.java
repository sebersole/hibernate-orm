/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.LoadingCollectionEntry;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * Base support for CollectionInitializer implementations that represent
 * an immediate initialization of some sort (join, select, batch, sub-select)
 * for a persistent collection.
 *
 * @implNote Mainly an intention contract wrt the immediacy of the fetch.
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractImmediateCollectionInitializer extends AbstractCollectionInitializer {
	// per-row state
	private PersistentCollection collectionInstance;

	protected AbstractImmediateCollectionInitializer(
			PersistentCollectionDescriptor collectionDescriptor,
			FetchParentAccess parentAccess,
			boolean isJoinFetch,
			DomainResultAssembler keyAssembler) {
		super( collectionDescriptor, parentAccess, isJoinFetch, keyAssembler );
	}

	@Override
	public PersistentCollection getCollectionInstance() {
		return collectionInstance;
	}

	@Override
	public void resolve(RowProcessingState rowProcessingState) {
		if ( collectionInstance != null ) {
			return;
		}

		final SharedSessionContractImplementor session = rowProcessingState.getSession();
		final CollectionKey collectionKey = getCollectionKey();

		// see if we are in the process of loading that collection with this initializer
		// in other words, is this initializers the one responsible for loading
		// the collection values?
		final LoadingCollectionEntry loadingEntry = rowProcessingState.getJdbcValuesSourceProcessingState()
				.findLoadingCollectionLocally( getCollectionDescriptor(), collectionKey.getKey() );

		final boolean isLoading;
		if ( loadingEntry != null ) {
			isLoading = true;
			collectionInstance = loadingEntry.getCollectionInstance();
		}
		else {
			final PersistentCollection existing = session.getPersistenceContext().getCollection( collectionKey );
			if ( existing != null ) {
				isLoading = false;
				collectionInstance = existing;
			}
			else {
				if ( isJoinFetch() ) {
					isLoading = true;
					collectionInstance = getCollectionDescriptor().instantiateWrapper(
							session,
							collectionKey.getKey()
					);
					rowProcessingState.getJdbcValuesSourceProcessingState().registerLoadingCollection(
							getCollectionDescriptor(),
							collectionKey,
							new LoadingCollectionEntry(
									getCollectionDescriptor(),
									this,
									collectionKey.getKey(),
									collectionInstance
							)
					);
					collectionInstance.beforeInitialize( -1, getCollectionDescriptor() );
					collectionInstance.beginRead();
				}
				else {
					isLoading = false;
					// note : this call adds the collection to the PC, so we will find it
					// next time (`existing`) and not attempt to load values
					collectionInstance = getCollectionDescriptor().getLoader().load( collectionKey.getKey(), session );
				}
			}
		}

		if ( isLoading ) {
			if ( getCollectionFkValue() != null ) {
				// the row contains an element in the collection...
				readCollectionRow( rowProcessingState );
			}
		}
	}

	protected abstract void readCollectionRow(RowProcessingState rowProcessingState);

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		super.finishUpRow( rowProcessingState );
		collectionInstance = null;
	}
}
