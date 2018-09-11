/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.results.spi.CollectionInitializer;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * Base support for CollectionInitializer implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractCollectionInitializer implements CollectionInitializer {
	private final PersistentCollectionDescriptor collectionDescriptor;
	private final boolean isJoinFetch;
	private final DomainResultAssembler keyAssembler;

	// per-row state
	private CollectionKey collectionKey;

	@SuppressWarnings("WeakerAccess")
	protected AbstractCollectionInitializer(
			PersistentCollectionDescriptor collectionDescriptor,
			boolean isJoinFetch,
			DomainResultAssembler keyAssembler) {
		this.collectionDescriptor = collectionDescriptor;
		this.isJoinFetch = isJoinFetch;
		this.keyAssembler = keyAssembler;
	}

	protected PersistentCollectionDescriptor getCollectionDescriptor() {
		return collectionDescriptor;
	}

	public boolean isJoinFetch() {
		return isJoinFetch;
	}

	protected CollectionKey getCollectionKey() {
		return collectionKey;
	}

	@Override
	public PluralPersistentAttribute getFetchedAttribute() {
		return getCollectionDescriptor().getDescribedAttribute();
	}

	@Override
	public void hydrate(RowProcessingState rowProcessingState) {
		final Object key = keyAssembler.assemble(
				rowProcessingState,
				rowProcessingState.getJdbcValuesSourceProcessingState().getProcessingOptions()
		);

		if ( key == null ) {
			throw new HibernateException( "Collection key cannot be null : " + getFetchedAttribute().getNavigableRole().getFullPath() );
		}

		collectionKey = new CollectionKey( getFetchedAttribute().getPersistentCollectionDescriptor(), key );

		afterKeyHydrated( collectionKey, rowProcessingState );
	}

	protected void afterKeyHydrated(CollectionKey collectionKey, RowProcessingState rowProcessingState) {
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		collectionKey = null;
	}
}
