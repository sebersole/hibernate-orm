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
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.CollectionFetch;
import org.hibernate.sql.results.spi.CollectionInitializer;

/**
 * @author Steve Ebersole
 */
public class CollectionFetchImpl extends AbstractCollectionMappingNode implements CollectionFetch {
	private final FetchTiming fetchTiming;

	private final LockMode lockMode;

	private final CollectionInitializerProducer initializerProducer;

	public CollectionFetchImpl(
			FetchParent fetchParent,
			PluralPersistentAttribute describedAttribute,
			FetchTiming fetchTiming,
			String resultVariable,
			LockMode lockMode,
			DomainResult keyResult,
			CollectionInitializerProducer initializerProducer) {
		super( fetchParent, describedAttribute, resultVariable, keyResult );
		this.fetchTiming = fetchTiming;
		this.lockMode = lockMode;
		this.initializerProducer = initializerProducer;
	}

	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			Consumer<Initializer> collector,
			AssemblerCreationContext context,
			AssemblerCreationState creationState) {
		final CollectionInitializer initializer = initializerProducer.produceInitializer(
				parentAccess,
				lockMode,
				getCollectionKeyResult().createResultAssembler( collector, creationState, context ),
				collector,
				creationState,
				context
		);

		collector.accept( initializer );

		return new PluralAttributeAssemblerImpl( initializer );
	}

	@Override
	public FetchParent getFetchParent() {
		return super.getFetchParent();
	}

	@Override
	public PluralPersistentAttribute getFetchedNavigable() {
		return getPluralAttribute();
	}

	@Override
	public boolean isNullable() {
		return true;
	}
}
