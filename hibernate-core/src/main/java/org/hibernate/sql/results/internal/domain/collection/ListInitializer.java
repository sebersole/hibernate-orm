/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.metamodel.model.domain.internal.PersistentListDescriptorImpl;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class ListInitializer extends AbstractCollectionInitializer {
	private final FetchParentAccess parentAccess;
	private final DomainResultAssembler listIndexAssembler;
	private final DomainResultAssembler elementAssembler;

	// per-row state
	private PersistentList collectionInstance;

	public ListInitializer(
			PersistentListDescriptorImpl listDescriptor,
			FetchParentAccess parentAccess,
			boolean joined,
			DomainResultAssembler collectionKeyAssembler,
			DomainResultAssembler listIndexAssembler,
			DomainResultAssembler elementAssembler) {
		super( listDescriptor, joined, collectionKeyAssembler );
		this.parentAccess = parentAccess;
		this.listIndexAssembler = listIndexAssembler;
		this.elementAssembler = elementAssembler;
	}

	@Override
	public PersistentCollection getCollectionInstance() {
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
