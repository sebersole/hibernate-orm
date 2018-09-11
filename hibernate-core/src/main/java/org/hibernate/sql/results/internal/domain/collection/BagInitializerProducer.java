/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.model.domain.internal.PersistentBagDescriptorImpl;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.CollectionInitializer;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;

/**
 * @author Steve Ebersole
 */
public class BagInitializerProducer implements CollectionInitializerProducer {
	private final PersistentBagDescriptorImpl bagDescriptor;
	private final boolean isJoinFetch;
	private final DomainResult collectionIdResult;
	private final DomainResult elementResult;

	public BagInitializerProducer(
			PersistentBagDescriptorImpl bagDescriptor,
			boolean isJoinFetch,
			DomainResult collectionIdResult,
			DomainResult elementResult) {
		if ( bagDescriptor.getIdDescriptor() != null ) {
			assert collectionIdResult != null;
		}
		else {
			assert collectionIdResult == null;
		}

		this.bagDescriptor = bagDescriptor;
		this.isJoinFetch = isJoinFetch;
		this.collectionIdResult = collectionIdResult;
		this.elementResult = elementResult;
	}

	@Override
	public CollectionInitializer produceInitializer(
			FetchParentAccess parentAccess,
			LockMode lockMode,
			DomainResultAssembler collectionKeyAssembler,
			Consumer<Initializer> initializerConsumer,
			AssemblerCreationState creationState,
			AssemblerCreationContext creationContext) {
		final DomainResultAssembler elementAssembler = elementResult.createResultAssembler(
				initializerConsumer,
				creationState,
				creationContext
		);

		final DomainResultAssembler collectionIdAssembler;
		if ( bagDescriptor.getIdDescriptor() == null ) {
			collectionIdAssembler = null;
		}
		else {
			collectionIdAssembler = collectionIdResult.createResultAssembler(
					initializerConsumer,
					creationState,
					creationContext
			);
		}

		return new BagInitializer(
				bagDescriptor,
				parentAccess,
				isJoinFetch,
				collectionKeyAssembler,
				elementAssembler,
				collectionIdAssembler
		);
	}
}
