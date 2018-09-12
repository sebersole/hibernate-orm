/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.metamodel.model.domain.internal.PersistentBagDescriptorImpl;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class BagInitializer extends AbstractImmediateCollectionInitializer {
	private final DomainResultAssembler elementAssembler;
	private final DomainResultAssembler collectionIdAssembler;

	public BagInitializer(
			PersistentBagDescriptorImpl bagDescriptor,
			FetchParentAccess parentAccess,
			boolean isJoinFetch,
			DomainResultAssembler collectionKeyAssembler,
			DomainResultAssembler elementAssembler,
			DomainResultAssembler collectionIdAssembler) {
		super( bagDescriptor, parentAccess, isJoinFetch, collectionKeyAssembler );
		this.elementAssembler = elementAssembler;
		this.collectionIdAssembler = collectionIdAssembler;
	}

	@Override
	public PersistentBag getCollectionInstance() {
		return (PersistentBag) super.getCollectionInstance();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void readCollectionRow(RowProcessingState rowProcessingState) {
		getCollectionInstance().load( elementAssembler.assemble( rowProcessingState ) );
	}
}
