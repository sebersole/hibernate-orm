/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.metamodel.model.domain.internal.PersistentBagDescriptorImpl;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class BagInitializer extends AbstractCollectionInitializer {
	private final FetchParentAccess parentAccess;
	private final DomainResultAssembler elementAssembler;
	private final DomainResultAssembler collectionIdAssembler;

	// per-row state
	private PersistentBag collectionInstance;

	public BagInitializer(
			PersistentBagDescriptorImpl bagDescriptor,
			FetchParentAccess parentAccess,
			boolean isJoinFetch,
			DomainResultAssembler collectionKeyAssembler,
			DomainResultAssembler elementAssembler,
			DomainResultAssembler collectionIdAssembler) {
		super( bagDescriptor, isJoinFetch, collectionKeyAssembler );
		this.parentAccess = parentAccess;
		this.elementAssembler = elementAssembler;
		this.collectionIdAssembler = collectionIdAssembler;
	}

	@Override
	public PersistentBag getCollectionInstance() {
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
