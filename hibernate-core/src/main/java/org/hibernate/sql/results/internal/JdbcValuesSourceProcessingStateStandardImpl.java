/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.graph.spi.AttributeNodeContainer;
import org.hibernate.graph.spi.AttributeNodeImplementor;
import org.hibernate.graph.spi.EntityGraphImplementor;
import org.hibernate.metamodel.model.domain.spi.PersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.query.spi.EntityGraphQueryHint;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.sql.ast.produce.metamodel.spi.Fetchable;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingOptions;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.LoadingCollectionEntry;
import org.hibernate.sql.results.spi.LoadingEntityEntry;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class JdbcValuesSourceProcessingStateStandardImpl implements JdbcValuesSourceProcessingState {
	private static final Logger log = Logger.getLogger( JdbcValuesSourceProcessingStateStandardImpl.class );

	private final ExecutionContext executionContext;
	private final JdbcValuesSourceProcessingOptions processingOptions;

	private Map<EntityKey,LoadingEntityEntry> loadingEntityMap;
	private Map<PersistentCollectionDescriptor,Map<Object,LoadingCollectionEntry>> loadingCollectionMap;

	private FetchContext fetchContext;

	private final PreLoadEvent preLoadEvent;
	private final PostLoadEvent postLoadEvent;

	// todo (6.0) : "loading collections" as well?

	public JdbcValuesSourceProcessingStateStandardImpl(
			ExecutionContext executionContext,
			JdbcValuesSourceProcessingOptions processingOptions) {
		this.executionContext = executionContext;
		this.processingOptions = processingOptions;

		preLoadEvent  = new PreLoadEvent( (EventSource) executionContext.getSession() );
		postLoadEvent  = new PostLoadEvent( (EventSource) executionContext.getSession() );

		fetchContext = resolveFetchContext( executionContext.getQueryOptions().getEntityGraphQueryHint() );
	}

	private FetchContext resolveFetchContext(EntityGraphQueryHint hint) {
		if ( hint != null ) {
			switch ( hint.getType() ) {
				case LOAD:
				case FETCH: {
					// todo (6.0) : have a FetchContext impl per type?
					return new FetchContextImpl( hint );
				}
			}
		}

		return null;
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	@Override
	public QueryOptions getQueryOptions() {
		return executionContext.getQueryOptions();
	}

	@Override
	public JdbcValuesSourceProcessingOptions getProcessingOptions() {
		return processingOptions;
	}

	@Override
	public PreLoadEvent getPreLoadEvent() {
		return preLoadEvent;
	}

	@Override
	public PostLoadEvent getPostLoadEvent() {
		return postLoadEvent;
	}

	@Override
	public boolean fetching(PersistentAttribute attribute) {
		if ( fetchContext == null ) {
			return true;
		}
		return fetchContext.fetching( attribute );
	}

	public interface FetchContext {
		boolean fetching(PersistentAttribute attribute);
	}

	private class FetchContextImpl implements FetchContext {
		private final EntityGraphQueryHint.Type graphType;

		private AttributeNodeContainer currentContainer;

		public FetchContextImpl(EntityGraphQueryHint entityGraphHint) {
			this( entityGraphHint.getHintedGraph(), entityGraphHint.getType() );
		}

		public FetchContextImpl(EntityGraphImplementor<?> hintedGraph, EntityGraphQueryHint.Type type) {
			currentContainer = hintedGraph;
			graphType = type;
		}

		@Override
		public boolean fetching(PersistentAttribute attribute) {
			if ( ! Fetchable.class.isInstance( attribute ) ) {
				// basic attributes are always fetched for now - that matches behavior of
				// Hibernate prior to 6.  Eventually though we want to hook this in with
				// bytecode-enhanced laziness
				return true;
			}

			// if there is an entity-graph, see if it says we should eagerly load the attribute
			// else see if the attribute is configured for join fetching
			//
			// ^^ so long as we do not exceed max-fetch-depth

			final AttributeNodeImplementor attributeNode = currentContainer.findAttributeNode( attribute.getAttributeName() );

//			if ( entityGraphSaysToEagerLoad( attributeNode ) ) {
//				prepareForFetch( attributeNode )
//			}
//
//			if ( attributeNode == null ) {
//				return false;
//			}
//
//			final Map subGraphs = attributeNode.subGraphs();
//			if ( subGraphs == null || subGraphs.isEmpty() ) {
//				return
//			}
//			if ( !shouldFetch ) {
//				return false;
//			}
//
////			final Fetchable fetchable = (Fetchable) attribute;
////			fetchable.generateFetch( ... )
//
//
//			final AttributeNodeImplementor attributeNode = (fetchNodeStack.getCurrent().findAttributeNode( attribute.getAttributeName() )fetchNodeStack.getCurrent().findAttributeNode( attribute.getAttributeName() );
//			fetchNodeStack.push( . );

			return false;
		}
	}

	@Override
	public LoadingEntityEntry registerLoadingEntity(
			EntityKey entityKey,
			Function<EntityKey,LoadingEntityEntry> entryProducer) {
		if ( loadingEntityMap == null ) {
			loadingEntityMap = new HashMap<>();
		}

		final LoadingEntityEntry loadingEntity = loadingEntityMap.compute(
				entityKey,
				(key, existingValue) -> {
					if ( existingValue == null ) {
						log.debugf(
								"Generating LoadingEntityEntry registration : %s[id=%s]",
								entityKey.getEntityName(),
								entityKey.getIdentifier()
						);
						return entryProducer.apply( key );
					}
					else {
						log.debugf(
								"Attempt to add duplicate LoadingEntityEntry registration for same EntityKey [%s]",
								entityKey
						);
						return existingValue;
					}
				}
		);

		return loadingEntity;
	}

	@Override
	public LoadingEntityEntry findLoadingEntryLocally(EntityKey entityKey) {
		return loadingEntityMap == null ? null : loadingEntityMap.get( entityKey );
	}

	@Override
	public LoadingCollectionEntry registerLoadingCollection(
			PersistentCollectionDescriptor collectionDescriptor,
			Object collectionKey,
			Supplier<LoadingCollectionEntry> entryProducer) {
		Map<Object, LoadingCollectionEntry> entryByKeyMap;
		final LoadingCollectionEntry existing;
		if ( loadingCollectionMap == null ) {
			loadingCollectionMap = new HashMap<>();
			entryByKeyMap = null;
			existing = null;
		}
		else {
			entryByKeyMap = loadingCollectionMap.get( collectionDescriptor );
			existing = entryByKeyMap.get( collectionKey );
		}

		if ( entryByKeyMap == null ) {
			entryByKeyMap = new HashMap<>();
			loadingCollectionMap.put( collectionDescriptor, entryByKeyMap );
		}

		if ( existing != null ) {
			log.debugf(
					"Attempt to add duplicate LoadingCollectionEntry registration for same key [%s#%s]",
					collectionDescriptor.getNavigableRole().getFullPath(),
					collectionKey
			);

			return existing;
		}
		else {
			final LoadingCollectionEntry produced = entryProducer.get();
			entryByKeyMap.put( collectionKey, produced );
			return produced;
		}
	}

	@Override
	public LoadingCollectionEntry findLoadingCollectionLocally(
			PersistentCollectionDescriptor collectionDescriptor,
			Object key) {
		if ( loadingCollectionMap == null ) {
			return null;
		}

		final Map<Object, LoadingCollectionEntry> entryMap = loadingCollectionMap.get( collectionDescriptor );
		if ( entryMap == null ) {
			return null;
		}

		return entryMap.get( key );
	}

	@Override
	public SharedSessionContractImplementor getSession() {
		return executionContext.getSession();
	}

	@Override
	public void finishUp() {
		executionContext.getSession().getPersistenceContext().getLoadContexts().register( this );

		try {
			// for arrays, we should end the collection load beforeQuery resolving the entities, since the
			// actual array instances are not instantiated during loading
			finishLoadingArrays();

			// now finish loading the entities (2-phase load)
			performTwoPhaseLoad();

			// now we can finalize loading collections
			finishLoadingCollections();
		}
		finally {
			executionContext.getSession().getPersistenceContext().getLoadContexts().deregister( this );
		}
	}

	private void finishLoadingArrays() {
//		for ( CollectionReferenceInitializer arrayReferenceInitializer : arrayReferenceInitializers ) {
//			arrayReferenceInitializer.endLoading( context );
//		}
	}


	private void performTwoPhaseLoad() {
		if ( loadingEntityMap == null ) {
			return;
		}

		log.tracev( "Total objects hydrated: {0}", loadingEntityMap.size() );
	}

	@SuppressWarnings("SimplifiableIfStatement")
	private boolean isReadOnly() {
		if ( getQueryOptions().isReadOnly() != null ) {
			return getQueryOptions().isReadOnly();
		}

		if ( executionContext.getSession() instanceof EventSource ) {
			return executionContext.getSession().isDefaultReadOnly();
		}

		return false;
	}


	private void finishLoadingCollections() {
//		for ( InitializerCollection initializer : initializers ) {
//			initializer.endLoading( context );
//		}

		// todo (6.0) : need something like org.hibernate.sql.results.spi.LoadingCollectionEntry
		//		^^ see new `org.hibernate.sql.results.spi.LoadContexts` &&

//		throw new NotYetImplementedFor6Exception(  );
	}

}
