/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.cache;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.internal.SimpleCacheKeysFactory;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.CacheableRegion;
import org.hibernate.cache.spi.CacheableRegionNameMapping;
import org.hibernate.cache.spi.RegionBuildingContext;
import org.hibernate.cache.spi.RequestedCollectionCaching;
import org.hibernate.cache.spi.RequestedEntityCaching;
import org.hibernate.cache.spi.RequestedNaturalIdCaching;
import org.hibernate.cache.spi.access.CollectionStorageAccess;
import org.hibernate.cache.spi.access.EntityStorageAccess;
import org.hibernate.cache.spi.access.NaturalIdStorageAccess;
import org.hibernate.cache.spi.access.UnknownAccessTypeException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.common.NavigableRole;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class CacheableRegionImpl implements CacheableRegion {
	private static final Logger log = Logger.getLogger( CacheableRegionImpl.class );

	private final CachingRegionFactory regionFactory;
	private final String name;
	// todo : enforced AccessType?

	private final Map<NavigableRole,EntityStorageAccess> entityRegionAccessByRootEntityName;
	private final Map<NavigableRole,CollectionStorageAccess> collectionRegionAccessByRole;
	private final Map<NavigableRole,NaturalIdStorageAccess> naturalIdRegionAccessByRootEntityName;

	private final ConcurrentHashMap cacheDataMap;

	public CacheableRegionImpl(
			CachingRegionFactory regionFactory,
			String name,
			CacheableRegionNameMapping regionNameMapping,
			RegionBuildingContext buildingContext) {
		this.regionFactory = regionFactory;
		this.name = name;

		this.entityRegionAccessByRootEntityName = generateEntityRegionAccessMap( regionNameMapping, buildingContext, this );
		this.collectionRegionAccessByRole = generateCollectionRegionAccessMap( regionNameMapping, buildingContext, this );
		this.naturalIdRegionAccessByRootEntityName = generateNaturalIdRegionAccessMap( regionNameMapping, buildingContext, this );

		this.cacheDataMap = new ConcurrentHashMap();
	}

	public CachingRegionFactory getRegionFactory() {
		return regionFactory;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void destroy() throws CacheException {
		// nothing to do here
	}

	@Override
	public EntityStorageAccess getEntityStorageAccess(NavigableRole rootEntityRole) {
		return entityRegionAccessByRootEntityName.get( rootEntityRole );
	}

	public CollectionStorageAccess getCollectionStorageAccess(NavigableRole collectionRole) {
		return collectionRegionAccessByRole.get( collectionRole );
	}

	public NaturalIdStorageAccess getNaturalIdStorageAccess(NavigableRole rootEntityRole) {
		return naturalIdRegionAccessByRootEntityName.get( rootEntityRole );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// For use by the access delegates

	public boolean contains(Object key) {
		return cacheDataMap.contains( key );
	}

	public Object get(SharedSessionContractImplementor session, Object key) {
		log.debugf( "Lookup (in Region [%s]), key : [%s]", getName(), key );
		if ( key == null ) {
			return null;
		}
		Object result = cacheDataMap.get( key );
		if ( result != null ) {
			log.debugf( "Cache hit (in Region [%]), key : %s", getName(), key );
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void put(SharedSessionContractImplementor session, Object key, Object value) {
		log.debugf( "Caching (into Region [%s]) : [%s] -> [%s]", getName(), key, value );
		if ( key == null || value == null ) {
			log.debug( "Key or Value is null" );
			return;
		}
		cacheDataMap.put( key, value );
	}

	public void evict(Object key) {
		log.debugf( "Evicting Region [%s], key: %s", getName(), key );
		if ( key == null ) {
			log.debug( "Key is null" );
			return;
		}
		cacheDataMap.remove( key );
	}

	public void evictAll() {
		log.debugf( "Evict Region [%s]", getName() );
		cacheDataMap.clear();
	}



	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// initialization code

	private static Map<NavigableRole, EntityStorageAccess> generateEntityRegionAccessMap(
			CacheableRegionNameMapping regionNameMapping,
			RegionBuildingContext buildingContext,
			CacheableRegionImpl region) {
		if ( regionNameMapping.getRequestedEntityCachingList().isEmpty() ) {
			return Collections.emptyMap();
		}

		// NOTE : notice how the different CacheKeysFactory instances are picked,
		// 		depending on whether there are multiple entity references or just one...

		if ( regionNameMapping.getRequestedEntityCachingList().size() == 1 ) {
			final RequestedEntityCaching requestedEntityCaching = regionNameMapping.getRequestedEntityCachingList().get( 0 );
			return Collections.singletonMap(
					requestedEntityCaching.getRootEntityNavigableRole(),
					createEntityRegionAccess(
							requestedEntityCaching,
							SimpleCacheKeysFactory.INSTANCE,
							region,
							buildingContext
					)
			);
		}

		final ConcurrentHashMap<NavigableRole, EntityStorageAccess> entityRegionAccessByRootEntityName = new ConcurrentHashMap<>();
		for ( RequestedEntityCaching requestedEntityCaching : regionNameMapping.getRequestedEntityCachingList() ) {
			entityRegionAccessByRootEntityName.put(
					requestedEntityCaching.getRootEntityNavigableRole(),
					createEntityRegionAccess(
							requestedEntityCaching,
							buildingContext.getEnforcedCacheKeysFactory() != null
									? buildingContext.getEnforcedCacheKeysFactory()
									: DefaultCacheKeysFactory.INSTANCE,
							region,
							buildingContext
					)
			);
		}
		return entityRegionAccessByRootEntityName;
	}

	private static EntityStorageAccess createEntityRegionAccess(
			RequestedEntityCaching metadata,
			CacheKeysFactory impliedCacheKeysFactory,
			CacheableRegionImpl regionImpl,
			RegionBuildingContext buildingContext) {
		final CacheKeysFactory factoryToUse = buildingContext.getEnforcedCacheKeysFactory() != null
				? buildingContext.getEnforcedCacheKeysFactory()
				: impliedCacheKeysFactory;

		switch ( metadata.getAccessType() ) {
			case NONSTRICT_READ_WRITE: {
				return new NonstrictReadWriteEntityRegionAccess(
						metadata.getRootEntityNavigableRole(),
						factoryToUse,
						regionImpl
				);
			}
			case READ_WRITE: {
				return new ReadWriteEntityRegionAccess( metadata, factoryToUse, regionImpl );
			}
			case READ_ONLY: {
				return new ReadOnlyEntityRegionAccess( metadata, factoryToUse, regionImpl );
			}
			case TRANSACTIONAL: {
				return new TransactionalEntityRegionAccess( metadata, factoryToUse, regionImpl );
			}
			default: {
				throw new UnknownAccessTypeException( metadata.getAccessType().name() );
			}
		}
	}

	private static Map<NavigableRole, CollectionStorageAccess> generateCollectionRegionAccessMap(
			CacheableRegionNameMapping regionNameMapping,
			RegionBuildingContext buildingContext,
			CacheableRegionImpl region) {
		if ( regionNameMapping.getRequestedCollectionCachingList().isEmpty() ) {
			return Collections.emptyMap();
		}

		// NOTE : notice how the different CacheKeysFactory instances are picked,
		// 		depending on whether there are multiple collection references or just one...

		if ( regionNameMapping.getRequestedCollectionCachingList().size() == 1 ) {
			final RequestedCollectionCaching requestedCollectionCaching = regionNameMapping.getRequestedCollectionCachingList().get( 0 );
			return Collections.singletonMap(
					requestedCollectionCaching.getCachedRole(),
					createCollectionRegionAccess(
							requestedCollectionCaching,
							SimpleCacheKeysFactory.INSTANCE,
							region
					)
			);
		}

		final ConcurrentHashMap<NavigableRole, CollectionStorageAccess> collectionRegionAccessByRole = new ConcurrentHashMap<>();
		for ( RequestedCollectionCaching requestedCollectionCaching : regionNameMapping.getRequestedCollectionCachingList() ) {
			collectionRegionAccessByRole.put(
					requestedCollectionCaching.getCachedRole(),
					createCollectionRegionAccess(
							requestedCollectionCaching,
							buildingContext.getEnforcedCacheKeysFactory() != null
									? buildingContext.getEnforcedCacheKeysFactory()
									: DefaultCacheKeysFactory.INSTANCE,
							region
					)
			);
		}
		return collectionRegionAccessByRole;
	}

	private static CollectionStorageAccess createCollectionRegionAccess(
			RequestedCollectionCaching requestedCollectionCaching,
			CacheKeysFactory factoryToUse,
			CacheableRegionImpl region) {
		switch( requestedCollectionCaching.getAccessType() ) {
			case NONSTRICT_READ_WRITE: {
				return new NonstrictReadWriteCollectionRegionAccess( requestedCollectionCaching, factoryToUse, region );
			}
			case READ_WRITE: {
				return new ReadWriteCollectionRegionAccess( requestedCollectionCaching, factoryToUse, region );
			}
			case READ_ONLY: {
				return new ReadOnlyCollectionRegionAccess( requestedCollectionCaching, factoryToUse, region );
			}
			case TRANSACTIONAL: {
				return new TransactionalCollectionRegionAccess( requestedCollectionCaching, factoryToUse, region );
			}
			default: {
				throw new UnknownAccessTypeException( requestedCollectionCaching.getAccessType().name() );
			}
		}

	}

	private static Map<NavigableRole, NaturalIdStorageAccess> generateNaturalIdRegionAccessMap(
			CacheableRegionNameMapping regionNameMapping,
			RegionBuildingContext buildingContext,
			CacheableRegionImpl region) {
		if ( regionNameMapping.getRequestedNaturalIdCachingList().isEmpty() ) {
			return Collections.emptyMap();
		}

		// NOTE : notice how the different CacheKeysFactory instances are picked,
		// 		depending on whether there are multiple natural-id references or just one...

		if ( regionNameMapping.getRequestedCollectionCachingList().size() == 1 ) {
			final RequestedNaturalIdCaching requestedNaturalIdCaching = regionNameMapping.getRequestedNaturalIdCachingList().get( 0 );
			return Collections.singletonMap(
					requestedNaturalIdCaching.getCachedRole(),
					createNaturalIdRegionAccess(
							requestedNaturalIdCaching,
							SimpleCacheKeysFactory.INSTANCE,
							region
					)
			);
		}

		final ConcurrentHashMap<NavigableRole, NaturalIdStorageAccess> naturalIdByRootEntityName = new ConcurrentHashMap<>();
		for ( RequestedNaturalIdCaching requestedNaturalIdCaching : regionNameMapping.getRequestedNaturalIdCachingList() ) {
			naturalIdByRootEntityName.put(
					requestedNaturalIdCaching.getRootEntityNavigableRole(),
					createNaturalIdRegionAccess(
							requestedNaturalIdCaching,
							buildingContext.getEnforcedCacheKeysFactory() != null
									? buildingContext.getEnforcedCacheKeysFactory()
									: DefaultCacheKeysFactory.INSTANCE,
							region
					)
			);
		}
		return naturalIdByRootEntityName;
	}

	private static NaturalIdStorageAccess createNaturalIdRegionAccess(
			RequestedNaturalIdCaching requestedNaturalIdCaching,
			CacheKeysFactory factoryToUse,
			CacheableRegionImpl region) {
		switch ( requestedNaturalIdCaching.getAccessType() ) {
			case NONSTRICT_READ_WRITE: {
				return new NonstrictReadWriteNaturalIdRegionAccess( requestedNaturalIdCaching, factoryToUse, region );
			}
			case READ_WRITE: {
				return new ReadWriteNaturalIdRegionAccess( requestedNaturalIdCaching, factoryToUse, region );
			}
			case READ_ONLY: {
				return new ReadOnlyNaturalIdRegionAccess( requestedNaturalIdCaching, factoryToUse, region );
			}
			case TRANSACTIONAL: {
				return new TransactionalNaturalIdRegionAccess( requestedNaturalIdCaching, factoryToUse, region );
			}
			default: {
				throw new UnknownAccessTypeException( requestedNaturalIdCaching.getAccessType().name() );
			}
		}
	}
}
