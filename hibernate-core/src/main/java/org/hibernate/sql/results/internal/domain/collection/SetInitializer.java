/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class SetInitializer extends AbstractCollectionInitializer {
	private final FetchParentAccess parentAccess;
	private final DomainResultAssembler elementAssembler;

	// per-row state
	private PersistentSet collectionInstance;

	public SetInitializer(
			PersistentCollectionDescriptor setDescriptor,
			FetchParentAccess parentAccess,
			boolean joined,
			DomainResultAssembler collectionKeyAssembler,
			DomainResultAssembler elementAssembler) {
		super( setDescriptor, joined, collectionKeyAssembler );
		this.parentAccess = parentAccess;
		this.elementAssembler = elementAssembler;
	}

	@Override
	public PersistentSet getCollectionInstance() {
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
