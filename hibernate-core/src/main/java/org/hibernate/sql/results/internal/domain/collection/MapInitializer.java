/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * Represents an immediate initialization of some sort (join, select, batch, sub-select)
 * of a persistent Map valued attribute.
 *
 * @see DelayedCollectionInitializer
 *
 * @author Steve Ebersole
 */
public class MapInitializer extends AbstractCollectionInitializer {
	private final FetchParentAccess fetchParentAccess;
	private final DomainResultAssembler mapKeyAssembler;
	private final DomainResultAssembler mapValueAssembler;

	// per-row state
	private PersistentMap collectionInstance;

	public MapInitializer(
			PersistentCollectionDescriptor collectionDescriptor,
			FetchParentAccess fetchParentAccess,
			boolean joined,
			DomainResultAssembler keyAssembler,
			DomainResultAssembler mapKeyAssembler,
			DomainResultAssembler mapValueAssembler) {
		super( collectionDescriptor, joined, keyAssembler );
		this.fetchParentAccess = fetchParentAccess;
		this.mapKeyAssembler = mapKeyAssembler;
		this.mapValueAssembler = mapValueAssembler;
	}

	@Override
	public PersistentMap getCollectionInstance() {
		return collectionInstance;
	}

	@Override
	public void resolve(RowProcessingState rowProcessingState) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		super.finishUpRow( rowProcessingState );
		collectionInstance = null;
	}
}
