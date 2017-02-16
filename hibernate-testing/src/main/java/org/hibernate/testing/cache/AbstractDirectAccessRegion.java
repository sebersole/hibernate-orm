/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.DirectAccessRegion;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractDirectAccessRegion implements DirectAccessRegion {
	private static final Logger log = Logger.getLogger( AbstractDirectAccessRegion.class );

	private final CachingRegionFactory regionFactory;
	private final String name;

	private final ConcurrentHashMap cacheDataMap;

	public AbstractDirectAccessRegion(CachingRegionFactory regionFactory, String name) {
		this.regionFactory = regionFactory;
		this.name = name;

		this.cacheDataMap = new ConcurrentHashMap();
	}

	public String getName() {
		return name;
	}

	@Override
	public Object get(SharedSessionContractImplementor session, Object key) throws CacheException {
		log.debugf( "DirectAccessRegion[%s] lookup : [%s]", getName(), key );
		if ( key == null ) {
			return null;
		}
		Object result = cacheDataMap.get( key );
		if ( result != null ) {
			log.debugf( "DirectAccessRegion[%s] hit: %s", getName(), key );
		}
		return result;
	}

	@Override
	public void put(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		log.debugf( "DirectAccessRegion[%s] put : [%s] -> [%s]", getName(), key, value );
		if ( key == null || value == null ) {
			log.debug( "Key or Value is null" );
			return;
		}
		cacheDataMap.put( key, value );
	}

	@Override
	public void evict(Object key) throws CacheException {
		cacheDataMap.remove( key );
	}

	@Override
	public void evictAll() throws CacheException {
		cacheDataMap.clear();
	}

	@Override
	public void destroy() throws CacheException {
		cacheDataMap.clear();
	}
}
