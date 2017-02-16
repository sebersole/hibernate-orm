/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheableRegion;
import org.hibernate.cache.spi.access.CacheableStorageAccess;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.common.NavigableRole;

import org.jboss.logging.Logger;

/**
 * @author Strong Liu
 */
public abstract class BaseRegionAccess implements CacheableStorageAccess {
	private static final Logger log = Logger.getLogger( BaseRegionAccess.class );

	private final CacheableRegionImpl region;
	private final NavigableRole navigableRole;

	public BaseRegionAccess(CacheableRegionImpl region, NavigableRole navigableRole) {
		this.region = region;
		this.navigableRole = navigableRole;
	}

	@Override
	public CacheableRegion getRegion() {
		return region;
	}

	protected CacheableRegionImpl getInternalRegion() {
		return region;
	}

	@Override
	public NavigableRole getAccessedNavigableRole() {
		return navigableRole;
	}

	protected abstract boolean isDefaultMinimalPutOverride();

	@Override
	public Object get(SharedSessionContractImplementor session, Object key) throws CacheException {
		return getInternalRegion().get( session, key );
	}

	@Override
	public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {
		return putFromLoad(session, key, value, version, isDefaultMinimalPutOverride() );
	}

	@Override
	public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, Object version, boolean minimalPutOverride)
			throws CacheException {

		if ( key == null || value == null ) {
			return false;
		}
		if ( minimalPutOverride && getInternalRegion().contains( key ) ) {
			log.debugf( "Item already cached: %s", key );
			return false;
		}
		log.debugf( "Caching: %s", key );
		getInternalRegion().put( session, key, value );
		return true;

	}

	@Override
	public SoftLock lockRegion() throws CacheException {
		return null;
	}

	@Override
	public void unlockRegion(SoftLock lock) throws CacheException {
		evictAll();
	}

	@Override
	public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {
		return null;
	}

	@Override
	public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {
	}

	@Override
	public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {
	}

	@Override
	public void removeAll() throws CacheException {
		evictAll();
	}

	@Override
	public void evict(Object key) throws CacheException {
		getInternalRegion().evict( key );
	}

	@Override
	public void evictAll() throws CacheException {
		getInternalRegion().evictAll();
	}
}
