/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.RequestedNaturalIdCaching;
import org.hibernate.cache.spi.access.CollectionStorageAccess;
import org.hibernate.cache.spi.access.EntityStorageAccess;
import org.hibernate.cache.spi.access.NaturalIdStorageAccess;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.spi.EntityPersister;

import org.jboss.logging.Logger;

/**
 * @author Eric Dalquist
 */
class BaseNaturalIdRegionAccess extends BaseRegionAccess implements NaturalIdStorageAccess {
	private static final Logger log = Logger.getLogger( BaseNaturalIdRegionAccess.class );

	private final CacheKeysFactory cacheKeysFactory;

	public BaseNaturalIdRegionAccess(
			RequestedNaturalIdCaching metadata,
			CacheKeysFactory cacheKeysFactory,
			CacheableRegionImpl region) {
		super( region, metadata.getRootEntityNavigableRole() );
		this.cacheKeysFactory = cacheKeysFactory;
	}

	@Override
	protected boolean isDefaultMinimalPutOverride() {
		return getInternalRegion().getRegionFactory().getSettings().isMinimalPutsEnabled();
	}

	@Override
	public Object get(SharedSessionContractImplementor session, Object key) throws CacheException {
		return getInternalRegion().get( session, key );
	}

	@Override
	public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {
		return putFromLoad( session, key, value, version, isDefaultMinimalPutOverride() );
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

	/**
	 * Region locks are not supported.
	 *
	 * @return <code>null</code>
	 *
	 * @see EntityStorageAccess#lockRegion()
	 * @see CollectionStorageAccess#lockRegion()
	 */
	@Override
	public SoftLock lockRegion() throws CacheException {
		return null;
	}

	/**
	 * Region locks are not supported - perform a cache clear as a precaution.
	 *
	 * @see EntityStorageAccess#unlockRegion(org.hibernate.cache.spi.access.SoftLock)
	 * @see CollectionStorageAccess#unlockRegion(org.hibernate.cache.spi.access.SoftLock)
	 */
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
	public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		return putFromLoad( session, key, value, null );
	}

	@Override
	public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		return false;
	}

	@Override
	public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		return putFromLoad( session, key, value, null );
	}

	@Override
	public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {
		return false;
	}

	/**
	 * A no-op since this is an asynchronous cache access strategy.
	 *
	 * @see #remove(SharedSessionContractImplementor, Object)
	 */
	@Override
	public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {
	}

	/**
	 * Called to evict data from the entire region
	 *
	 * @throws CacheException Propogated from underlying {@link org.hibernate.cache.spi.Region}
	 * @see EntityStorageAccess#removeAll()
	 * @see CollectionStorageAccess#removeAll()
	 */
	@Override
	public void removeAll() throws CacheException {
		evictAll();
	}

	@Override
	public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {
		return cacheKeysFactory.createNaturalIdKey( naturalIdValues, persister, session );
	}

	@Override
	public Object[] getNaturalIdValues(Object cacheKey) {
		return cacheKeysFactory.getNaturalIdValues( cacheKey );
	}
}
