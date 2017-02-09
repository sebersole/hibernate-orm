/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.CollectionCacheDataDescription;
import org.hibernate.cache.spi.EntityCacheDataDescription;
import org.hibernate.cache.spi.NaturalIdCacheDataDescription;
import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccess;
import org.hibernate.cache.spi.access.EntityRegionAccess;
import org.hibernate.cache.spi.access.NaturalIdRegionAccess;
import org.hibernate.cache.spi.access.QueryResultRegionAccess;
import org.hibernate.cache.spi.access.UnknownAccessTypeException;
import org.hibernate.cache.spi.access.UpdateTimestampsRegionAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class RegionImpl implements Region {
	private static final Logger log = Logger.getLogger( RegionImpl.class );

	private final String name;
	private final CacheKeysFactory cacheKeysFactory;
	private final CachingRegionFactory regionFactory;
	private final ConcurrentHashMap cacheDataMap;


	public RegionImpl(String name, CacheKeysFactory cacheKeysFactory, CachingRegionFactory regionFactory) {
		this.name = name;
		this.cacheKeysFactory = cacheKeysFactory;
		this.regionFactory = regionFactory;
		this.cacheDataMap = new ConcurrentHashMap<>();
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
	public EntityRegionAccess buildEntityRegionAccess(
			AccessType accessType,
			EntityCacheDataDescription metadata) {
		switch ( accessType ) {
			case NONSTRICT_READ_WRITE: {
				return new NonstrictReadWriteEntityRegionAccess( metadata, cacheKeysFactory, this );
			}
			case READ_WRITE: {
				return new ReadWriteEntityRegionAccess( metadata, cacheKeysFactory, this );
			}
			case READ_ONLY: {
				return new ReadOnlyEntityRegionAccess( metadata, cacheKeysFactory, this );
			}
			case TRANSACTIONAL: {
				return new TransactionalEntityRegionAccess( metadata, cacheKeysFactory, this );
			}
			default: {
				throw new UnknownAccessTypeException( accessType.name() );
			}
		}
	}

	@Override
	public NaturalIdRegionAccess buildNaturalIdRegionAccess(
			AccessType accessType,
			NaturalIdCacheDataDescription metadata) {
		switch ( accessType ) {
			case NONSTRICT_READ_WRITE: {
				return new NonstrictReadWriteNaturalIdRegionAccess( metadata, cacheKeysFactory, this );
			}
			case READ_WRITE: {
				return new ReadWriteNaturalIdRegionAccess( metadata, cacheKeysFactory, this );
			}
			case READ_ONLY: {
				return new ReadOnlyNaturalIdRegionAccess( metadata, cacheKeysFactory, this );
			}
			case TRANSACTIONAL: {
				return new TransactionalNaturalIdRegionAccess( metadata, cacheKeysFactory, this );
			}
			default: {
				throw new UnknownAccessTypeException( accessType.name() );
			}
		}
	}

	@Override
	public CollectionRegionAccess buildCollectionAccess(
			AccessType accessType,
			CollectionCacheDataDescription metadata) throws CacheException {
		switch ( accessType ) {
			case NONSTRICT_READ_WRITE: {
				return new NonstrictReadWriteCollectionRegionAccess( metadata, cacheKeysFactory, this );
			}
			case READ_WRITE: {
				return new ReadWriteCollectionRegionAccess( metadata, cacheKeysFactory, this );
			}
			case READ_ONLY: {
				return new ReadOnlyCollectionRegionAccess( metadata, cacheKeysFactory, this );
			}
			case TRANSACTIONAL: {
				return new TransactionalCollectionRegionAccess( metadata, cacheKeysFactory, this );
			}
			default: {
				throw new UnknownAccessTypeException( accessType.name() );
			}
		}
	}

	@Override
	public QueryResultRegionAccess buildQueryResultRegionAccess() {
		return new QueryResultRegionAccessImpl( this );
	}

	@Override
	public UpdateTimestampsRegionAccess buildUpdateTimestampsRegionAccess() {
		return new UpdateTimestampsRegionAccessImpl( this );
	}


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
}
