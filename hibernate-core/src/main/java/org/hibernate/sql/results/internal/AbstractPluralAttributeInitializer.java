/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.function.Consumer;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.LoadContexts;
import org.hibernate.sql.results.spi.LoadingCollectionEntry;
import org.hibernate.sql.results.spi.PluralAttributeInitializer;
import org.hibernate.sql.results.spi.PluralAttributeMappingNode;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractPluralAttributeInitializer implements PluralAttributeInitializer {
	private final PluralPersistentAttribute attribute;

	private final DomainResultAssembler keyAssembler;
	private final DomainResultAssembler identifierAssembler;
	private final DomainResultAssembler indexAssembler;
	private final DomainResultAssembler elementAssembler;

	private boolean loading;
	private LoadingCollectionEntry loadingCollectionEntry;


	public AbstractPluralAttributeInitializer(
			PluralAttributeMappingNode resultDescriptor,
			Consumer<Initializer> initializerConsumer,
			AssemblerCreationState creationState,
			AssemblerCreationContext creationContext) {
		this.attribute = resultDescriptor.getCollectionDescriptor().getDescribedAttribute();

		this.keyAssembler = resultDescriptor.getKeyResult().createResultAssembler(
				initializer -> { throw new UnsupportedOperationException(); },
				creationState,
				creationContext
		);

		if ( resultDescriptor.getIdentifierResult() != null ) {
			this.identifierAssembler = resultDescriptor.getIdentifierResult().createResultAssembler(
					initializer -> { throw new UnsupportedOperationException(); },
					creationState,
					creationContext
			);
		}
		else {
			this.identifierAssembler = null;
		}

		if ( resultDescriptor.getIndexResult() != null ) {
			this.indexAssembler = resultDescriptor.getIndexResult().createResultAssembler(
					initializerConsumer,
					creationState,
					creationContext
			);
		}
		else {
			this.indexAssembler = null;
		}

		if ( resultDescriptor.getElementResult() != null ) {
			this.elementAssembler = resultDescriptor.getElementResult().createResultAssembler(
					initializerConsumer,
					creationState,
					creationContext
			);
		}
		else {
			this.elementAssembler = null;
		}
	}

	@Override
	public PluralPersistentAttribute getFetchedAttribute() {
		return attribute;
	}

	@Override
	public PersistentCollection getCollectionInstance() {
		return loadingCollectionEntry.getCollectionInstance();
	}

	@Override
	public void hydrate(RowProcessingState rowProcessingState) {
		final Object key = keyAssembler.assemble(
				rowProcessingState,
				rowProcessingState.getJdbcValuesSourceProcessingState().getProcessingOptions()
		);

		final PersistentCollectionDescriptor collectionDescriptor = getFetchedAttribute().getPersistentCollectionDescriptor();

		final SharedSessionContractImplementor session = rowProcessingState.getSession();
		final LoadContexts loadContexts = session.getPersistenceContext().getLoadContexts();

		loadingCollectionEntry = loadContexts.findLoadingCollectionEntry(
				collectionDescriptor,
				key
		);

		if ( loadingCollectionEntry == null ) {
			loadingCollectionEntry = new LoadingCollectionEntry(
					collectionDescriptor,
					key,
					collectionDescriptor.instantiateWrapper( session, key )
			);
			loading = true;
			rowProcessingState.getJdbcValuesSourceProcessingState().registerLoadingCollection(
					collectionDescriptor,
					key,
					loadingCollectionEntry
			);
		}
	}

	@Override
	public void resolve(RowProcessingState rowProcessingState) {
		assert loadingCollectionEntry != null;

		// todo (6.0) : flesh this out...
		//		this is where element, key and identifier values should
		// 		be read and the entry made into the loading collection

		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		loadingCollectionEntry = null;
		loading = false;
	}
}
