/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.WrongClassException;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionEventListenerManager;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.internal.util.MarkerObject;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.EntityInitializer;
import org.hibernate.sql.results.spi.EntityMappingNode;
import org.hibernate.sql.results.spi.Fetch;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingOptions;
import org.hibernate.sql.results.spi.LoadingEntityEntry;
import org.hibernate.sql.results.spi.RowProcessingState;
import org.hibernate.type.internal.TypeHelper;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractEntityInitializer implements EntityInitializer {
	private static final Logger log = Logger.getLogger( AbstractEntityInitializer.class );
	private static final boolean debugEnabled = log.isDebugEnabled();


	// NOTE : even though we only keep the EntityDescriptor here, rather than EntityReference
	//		the "scope" of this initializer is a specific EntityReference.
	//
	//		The full EntityReference is simply not needed here, and so we just keep
	//		the EntityDescriptor here to avoid chicken/egg issues in the coe creating
	// 		these

	private final EntityDescriptor<?> entityDescriptor;
	private final LockMode lockMode;

	private final List<Initializer> identifierInitializers = new ArrayList<>();

	private final DomainResultAssembler identifierAssembler;
	private final DomainResultAssembler discriminatorAssembler;
	private final DomainResultAssembler versionAssembler;

	private final Map<StateArrayContributor, DomainResultAssembler> assemblerMap = new HashMap<>();

	// in-flight processing state.  reset after each row
	private EntityDescriptor <?> concreteDescriptor;
	private EntityKey entityKey;

	private Object entityInstance;

	private LoadingEntityEntry loadingEntityEntry;

	private boolean injectEntityState = true;


	@SuppressWarnings("WeakerAccess")
	protected AbstractEntityInitializer(
			EntityMappingNode resultDescriptor,
			LockMode lockMode,
			Consumer<Initializer> initializerConsumer,
			AssemblerCreationContext context,
			AssemblerCreationState creationState) {
		super( );
		this.entityDescriptor = resultDescriptor.getEntityDescriptor();
		this.lockMode = lockMode;

		this.identifierAssembler = resultDescriptor.getIdentifierResult().createResultAssembler(
				identifierInitializers::add,
				creationState,
				context
		);

		if ( entityDescriptor.getHierarchy().getDiscriminatorDescriptor() != null ) {
			assert resultDescriptor.getDiscriminatorResult() != null;
			discriminatorAssembler = resultDescriptor.getDiscriminatorResult().createResultAssembler(
					initializer -> { throw new UnsupportedOperationException( "Registering an Initializer as part of Entity discriminator is illegal" ); },
					creationState,
					context
			);
		}
		else {
			discriminatorAssembler = null;
		}

		if ( entityDescriptor.getHierarchy().getVersionDescriptor() != null ) {
			assert resultDescriptor.getVersionResult() != null;
			this.versionAssembler = resultDescriptor.getVersionResult().createResultAssembler(
					initializer -> { throw new UnsupportedOperationException( "Registering an Initializer as part of Entity version is illegal" ); },
					creationState,
					context
			);
		}
		else {
			this.versionAssembler = null;
		}

		entityDescriptor.visitStateArrayContributors(
				stateArrayContributor -> {
					// todo (6.0) : somehow we need to track whether all state is loaded/resolved
					//		note that lazy proxies or uninitialized collections count against
					//		that in the affirmative

					final Fetch fetch = resultDescriptor.findFetch( stateArrayContributor.getNavigableName() );

					final DomainResultAssembler stateAssembler;
					if ( fetch == null ) {
						stateAssembler = new NullValueAssembler( stateArrayContributor.getJavaTypeDescriptor() );
					}
					else {
						stateAssembler = fetch.createAssembler( this, initializerConsumer, context, creationState );
					}

					assemblerMap.put( stateArrayContributor, stateAssembler );
				}
		);
	}

	protected abstract boolean isEntityReturn();

	@Override
	public EntityDescriptor getEntityDescriptor() {
		return entityDescriptor;
	}

	@Override
	public Object getEntityInstance() {
		return entityInstance;
	}

	@Override
	public Object getFetchParentInstance() {
		if ( entityInstance == null ) {
			throw new IllegalStateException( "Unexpected state condition - entity instance not yet resolved" );
		}

		return entityInstance;
	}

	/**
	 * Should we consider this entity reference batchable?
	 */
	protected boolean shouldBatchFetch() {
		return true;
	}

	// From CollectionType.
	//		todo : expose CollectionType#NOT_NULL_COLLECTION as public
	private static final Object NOT_NULL_COLLECTION = new MarkerObject( "NOT NULL COLLECTION" );

	@Override
	public void hydrate(RowProcessingState rowProcessingState) {
		// todo (6.0) : atm we do not handle sequential selects
		// 		- see AbstractEntityPersister#hasSequentialSelect and
		//			AbstractEntityPersister#getSequentialSelect in 5.2

		// NOTE : The gist of this impl is as follows:
		//		1) we

		if ( entityInstance != null ) {
			return;
		}

		final SharedSessionContractImplementor session = rowProcessingState.getJdbcValuesSourceProcessingState().getSession();
		concreteDescriptor = resolveConcreteEntityDescriptor( rowProcessingState, session );

		hydrateIdentifier( rowProcessingState );
		resolveEntityKey( rowProcessingState );

		assert entityKey != null;

		this.loadingEntityEntry = session.getPersistenceContext()
				.getLoadContexts()
				.findLoadingEntityEntry( entityKey );

		if ( loadingEntityEntry != null ) {
			// the entity is already being loaded elsewhere
			setRowStateFromLoadingEntry( loadingEntityEntry );
			return;
		}

//		final Object rowId;
//		if ( concreteDescriptor.getHierarchy().getRowIdDescriptor() != null ) {
//			rowId = ro sqlSelectionMappings.getRowIdSqlSelection().hydrateStateArray( rowProcessingState );
//
//			if ( rowId == null ) {
//				throw new HibernateException(
//						"Could not read entity row-id from JDBC : " + entityKey
//				);
//			}
//		}
//		else {
//			rowId = null;
//		}

		// this isEntityReturn bit is just for entity loaders, not hql/criteria
		if ( isEntityReturn() ) {
			final Object requestedEntityId = rowProcessingState.getJdbcValuesSourceProcessingState().getProcessingOptions().getEffectiveOptionalId();
			if ( requestedEntityId != null && requestedEntityId.equals( entityKey.getIdentifier() ) ) {
				entityInstance = rowProcessingState.getJdbcValuesSourceProcessingState().getProcessingOptions().getEffectiveOptionalObject();
			}
		}

		if ( entityInstance == null ) {
			entityInstance = session.instantiate( concreteDescriptor.getEntityName(), entityKey.getIdentifier() );

			this.loadingEntityEntry = new LoadingEntityEntry(
					entityKey,
					concreteDescriptor,
					entityInstance,
					// todo (6.0) : rowId
					// rowId,
					null
			);

			rowProcessingState.getJdbcValuesSourceProcessingState().registerLoadingEntity(
					entityKey,
					loadingEntityEntry
			);
		}
	}

	public void hydrateIdentifier(RowProcessingState rowProcessingState) {
		identifierInitializers.forEach( initializer -> initializer.hydrate( rowProcessingState ) );
	}

	public void resolveEntityKey(RowProcessingState rowProcessingState) {
		if ( entityKey != null ) {
			// its already been resolved
			return;
		}

		final SharedSessionContractImplementor session = rowProcessingState.getJdbcValuesSourceProcessingState().getSession();

		//		1) resolve the hydrated identifier value(s) into its identifier representation
		final Object id  = identifierAssembler.assemble( rowProcessingState, rowProcessingState.getJdbcValuesSourceProcessingState().getProcessingOptions() );

		//		2) build the EntityKey
		this.entityKey = new EntityKey( id, concreteDescriptor.getEntityDescriptor() );

		//		3) schedule the EntityKey for batch loading, if possible
		if ( shouldBatchFetch() && concreteDescriptor.getEntityDescriptor().isBatchLoadable() ) {
			if ( !session.getPersistenceContext().containsEntity( entityKey ) ) {
				session.getPersistenceContext().getBatchFetchQueue().addBatchLoadableEntityKey( entityKey );
			}
		}

		// todo (6.0) : subselect fetches similar to batch fetch handling above
	}

	private void setRowStateFromLoadingEntry(LoadingEntityEntry loadingEntityEntry) {
		this.entityKey = loadingEntityEntry.getEntityKey();
		this.entityInstance = loadingEntityEntry.getEntityInstance();
		this.concreteDescriptor = loadingEntityEntry.getDescriptor();

		// we are not the ones controlling the Initialization of the entity instance
		this.injectEntityState = false;
	}

	private EntityDescriptor resolveConcreteEntityDescriptor(
			RowProcessingState rowProcessingState,
			SharedSessionContractImplementor persistenceContext) throws WrongClassException {
		if ( discriminatorAssembler == null ) {
			return entityDescriptor;
		}

		final Object discriminatorValue = discriminatorAssembler.assemble(
				rowProcessingState,
				rowProcessingState.getJdbcValuesSourceProcessingState().getProcessingOptions()
		);

		final String result = entityDescriptor.getHierarchy()
				.getDiscriminatorDescriptor()
				.getDiscriminatorMappings()
				.discriminatorValueToEntityName( discriminatorValue );

		if ( result == null ) {
			// oops - we got an instance of another class hierarchy branch
			throw new WrongClassException(
					"Discriminator: " + discriminatorValue,
					entityKey.getIdentifier(),
					entityDescriptor.getEntityName()
			);
		}

		return persistenceContext.getFactory().getMetamodel().findEntityDescriptor( result );
	}

	@Override
	public void resolve(RowProcessingState rowProcessingState) {
		if ( ! injectEntityState ) {
			// we are not the Initializer responsible for initializing
			// the given entity instance.  Skip all of this method
			return;
		}

		final Object entityIdentifier = entityKey.getIdentifier();

		final SharedSessionContractImplementor session = rowProcessingState.getJdbcValuesSourceProcessingState().getSession();
		final JdbcValuesSourceProcessingOptions processingOptions = rowProcessingState.getJdbcValuesSourceProcessingState() .getProcessingOptions();

		preLoad( rowProcessingState );

		// apply wrapping and conversions


		final Object[] entityState = new Object[ assemblerMap.size() ];
		assemblerMap.forEach(
				(key, value) -> entityState[ key.getStateArrayPosition() ] = value.assemble(
						rowProcessingState,
						processingOptions
				)
		);

		entityDescriptor.setIdentifier( entityInstance, entityIdentifier, session );

		entityDescriptor.setPropertyValues( entityInstance, entityState );


		session.getPersistenceContext().addEntity(
				entityKey,
				entityInstance
		);

		final Object version;
		if ( versionAssembler != null ) {
			version = versionAssembler.assemble( rowProcessingState, processingOptions );
		}
		else {
			version = null;
		}

		final EntityEntry entityEntry = session.getPersistenceContext().addEntry(
				entityInstance,
				Status.LOADING,
				entityState,
				loadingEntityEntry.getRowId(),
				entityKey.getIdentifier(),
				version,
				lockMode,
				true,
				entityDescriptor,
				false
		);

		final SessionFactoryImplementor factory = session.getFactory();
		final EntityDataAccess cacheAccess = entityDescriptor.getHierarchy().getEntityCacheAccess();
		if ( cacheAccess != null && session.getCacheMode().isPutEnabled() ) {

			if ( debugEnabled ) {
				log.debugf(
						"Adding entityInstance to second-level cache: %s",
						MessageHelper.infoString( entityDescriptor, entityIdentifier, session.getFactory() )
				);
			}

			final CacheEntry entry = entityDescriptor.buildCacheEntry( entityInstance, entityState, version, session );
			final Object cacheKey = cacheAccess.generateCacheKey(
					entityIdentifier,
					entityDescriptor.getHierarchy(),
					factory,
					session.getTenantIdentifier()
			);

			// explicit handling of caching for rows just inserted and then somehow forced to be read
			// from the database *within the same transaction*.  usually this is done by
			// 		1) Session#refresh, or
			// 		2) Session#clear + some form of load
			//
			// we need to be careful not to clobber the lock here in the cache so that it can be rolled back if need be
			if ( session.getPersistenceContext().wasInsertedDuringTransaction( entityDescriptor, entityIdentifier ) ) {
				cacheAccess.update(
						session,
						cacheKey,
						entityDescriptor.getCacheEntryStructure().structure( entry ),
						version,
						version
				);
			}
			else {
				final SessionEventListenerManager eventListenerManager = session.getEventListenerManager();
				try {
					eventListenerManager.cachePutStart();
					final boolean put = cacheAccess.putFromLoad(
							session,
							cacheKey,
							entityDescriptor.getCacheEntryStructure().structure( entry ),
							version,
							//useMinimalPuts( session, entityEntry )
							false
					);

					if ( put && factory.getStatistics().isStatisticsEnabled() ) {
						factory.getStatistics().entityCachePut( entityDescriptor.getNavigableRole(), cacheAccess.getRegion().getName() );
					}
				}
				finally {
					eventListenerManager.cachePutEnd();
				}
			}
		}

		if ( entityDescriptor.getHierarchy().getNaturalIdDescriptor() != null ) {
			session.getPersistenceContext().getNaturalIdHelper().cacheNaturalIdCrossReferenceFromLoad(
					entityDescriptor,
					entityIdentifier,
					session.getPersistenceContext().getNaturalIdHelper().extractNaturalIdValues( entityState, entityDescriptor )
			);
		}

		boolean isReallyReadOnly = isReadOnly( rowProcessingState, session );
		if ( !entityDescriptor.getHierarchy().getMutabilityPlan().isMutable() ) {
			isReallyReadOnly = true;
		}
		else {
			final Object proxy = session.getPersistenceContext().getProxy( loadingEntityEntry.getEntityKey() );
			if ( proxy != null ) {
				// there is already a proxy for this impl
				// only set the status to read-only if the proxy is read-only
				isReallyReadOnly = ( (HibernateProxy) proxy ).getHibernateLazyInitializer().isReadOnly();
			}
		}
		if ( isReallyReadOnly ) {
			//no need to take a snapshot - this is a
			//performance optimization, but not really
			//important, except for entities with huge
			//mutable property values
			session.getPersistenceContext().setEntryStatus( entityEntry, Status.READ_ONLY );
		}
		else {
			//take a snapshot
			TypeHelper.deepCopy(
					entityDescriptor,
					entityState,
					entityState,
					StateArrayContributor::isUpdatable
			);
			session.getPersistenceContext().setEntryStatus( entityEntry, Status.MANAGED );
		}

		entityDescriptor.afterInitialize( entityInstance, session );

		if ( debugEnabled ) {
			log.debugf(
					"Done materializing entityInstance %s",
					MessageHelper.infoString( entityDescriptor, entityIdentifier, session.getFactory() )
			);
		}

		if ( factory.getStatistics().isStatisticsEnabled() ) {
			factory.getStatistics().loadEntity( entityDescriptor.getEntityName() );
		}


		postLoad( rowProcessingState );
	}

	private boolean isReadOnly(
			RowProcessingState rowProcessingState,
			SharedSessionContractImplementor persistenceContext) {
		if ( persistenceContext.isDefaultReadOnly() ) {
			return true;
		}


		final Boolean queryOption = rowProcessingState.getJdbcValuesSourceProcessingState().getQueryOptions().isReadOnly();

		return queryOption == null ? false : queryOption;
	}

	private void preLoad(RowProcessingState rowProcessingState) {
		final SharedSessionContractImplementor session = rowProcessingState.getJdbcValuesSourceProcessingState().getSession();

		final PreLoadEvent preLoadEvent = rowProcessingState.getJdbcValuesSourceProcessingState().getPreLoadEvent();
		preLoadEvent.reset();

		// Must occur after resolving identifiers!
		if ( session.isEventSource() ) {
			preLoadEvent.setEntity( entityInstance )
					.setId( entityKey.getIdentifier() )
					.setDescriptor( entityDescriptor );

			final EventListenerGroup<PreLoadEventListener> listenerGroup = session.getFactory()
					.getServiceRegistry()
					.getService( EventListenerRegistry.class )
					.getEventListenerGroup( EventType.PRE_LOAD );
			for ( PreLoadEventListener listener : listenerGroup.listeners() ) {
				listener.onPreLoad( preLoadEvent );
			}
		}
	}

	private void postLoad(RowProcessingState rowProcessingState) {
		final PostLoadEvent postLoadEvent = rowProcessingState.getJdbcValuesSourceProcessingState().getPostLoadEvent();
		postLoadEvent.reset();

		postLoadEvent.setEntity( loadingEntityEntry.getEntityInstance() )
				.setId( loadingEntityEntry.getEntityKey().getIdentifier() )
				.setDescriptor( loadingEntityEntry.getDescriptor() );

		final EventListenerGroup<PostLoadEventListener> listenerGroup = entityDescriptor.getFactory()
				.getServiceRegistry()
				.getService( EventListenerRegistry.class )
				.getEventListenerGroup( EventType.POST_LOAD );
		for ( PostLoadEventListener listener : listenerGroup.listeners() ) {
			listener.onPostLoad( postLoadEvent );
		}
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		// reset row state
		concreteDescriptor = null;
		entityKey = null;
		entityInstance = null;
		loadingEntityEntry = null;
		injectEntityState = true;
	}
}
