/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.results.SqlResultsLogger;
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
	private final LockMode lockMode;
	// per-row state
	private PersistentCollection collectionInstance;

	public AbstractImmediateCollectionInitializer(
			PersistentCollectionDescriptor collectionDescriptor,
			FetchParentAccess parentAccess,
			NavigablePath navigablePath,
			boolean selected,
			LockMode lockMode,
			DomainResultAssembler keyTargetAssembler,
			DomainResultAssembler keyCollectionAssembler) {
		super( collectionDescriptor, parentAccess, navigablePath, selected, keyTargetAssembler, keyCollectionAssembler );
		this.lockMode = lockMode;
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
		final CollectionKey collectionKey = resolveCollectionKey( rowProcessingState );

		// see if we are in the process of loading that collection with this initializer
		// in other words, is this initializers the one responsible for loading
		// the collection values?
		final LoadingCollectionEntry existingLoadingEntry = rowProcessingState.getJdbcValuesSourceProcessingState()
				.findLoadingCollectionLocally( getCollectionDescriptor(), collectionKey.getKey() );

		if ( existingLoadingEntry != null ) {
			SqlResultsLogger.INSTANCE.debugf(
					"Found existing loading entry [%s - %s] - using loading collection instance",
					getNavigablePath().getFullPath(),
					collectionKey.getKey()
			);

			collectionInstance = existingLoadingEntry.getCollectionInstance();

			if ( existingLoadingEntry.getInitializer() != this ) {
				// the entity is already being loaded elsewhere
				SqlResultsLogger.INSTANCE.debugf(
						"Collection (%s, %s) being loaded by another initializer [%s] - skipping processing",
						getNavigablePath().getFullPath(),
						collectionKey.getKey(),
						existingLoadingEntry.getInitializer()
				);

				// EARLY EXIT!!!
				return;
			}
		}
		else {
			final PersistentCollection existing = session.getPersistenceContext().getCollection( collectionKey );
			if ( existing != null ) {
				collectionInstance = existing;

				SqlResultsLogger.INSTANCE.debugf(
						"Found existing Collection instance (%s, %s) in Session [%s] - skipping processing",
						getNavigablePath().getFullPath(),
						collectionKey.getKey(),
						existing
				);

				// EARLY EXIT!!!
				return;
			}
			if ( ! isSelected() ) {
				// note : this call adds the collection to the PC, so we will find it
				// next time (`existing`) and not attempt to load values
				collectionInstance = getCollectionDescriptor().getLoader().load(
						collectionKey.getKey(),
						new LockOptions( lockMode ),
						session
				);

				// EARLY EXIT!!!
				return;
			}
		}

		if ( collectionInstance == null ) {
			collectionInstance = getCollectionDescriptor().instantiateWrapper(
					session,
					collectionKey.getKey()
			);

			SqlResultsLogger.INSTANCE.debugf(
					"Created new collection wrapper (%s - %s) : %s",
					getNavigablePath().getFullPath(),
					collectionKey.getKey(),
					collectionInstance
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
		}

		if ( getKeyCollectionValue() != null ) {
			// the row contains an element in the collection...
			readCollectionRow( rowProcessingState );
		}
	}

	protected abstract void readCollectionRow(RowProcessingState rowProcessingState);

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		super.finishUpRow( rowProcessingState );
		collectionInstance = null;
	}
}
