/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import java.util.Comparator;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.NaturalIdCacheDataDescription;
import org.hibernate.cache.spi.access.NaturalIdRegionAccess;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.spi.EntityPersister;

/**
 * @author Eric Dalquist
 */
class ReadWriteNaturalIdRegionAccess extends AbstractReadWriteAccess implements NaturalIdRegionAccess {
	private final NaturalIdCacheDataDescription metadata;

	ReadWriteNaturalIdRegionAccess(
			NaturalIdCacheDataDescription metadata,
			CacheKeysFactory cacheKeysFactory,
			RegionImpl region) {
		super( cacheKeysFactory, region );
		this.metadata = metadata;
	}

	@Override
	public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		return false;
	}

	@Override
	public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		return false;
	}

	@Override
	public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {

		try {
			writeLock.lock();
			Lockable item = (Lockable) getInternalRegion().get( session, key );
			if ( item == null ) {
				getInternalRegion().put( session, key, new Item( value, null, getInternalRegion().getRegionFactory().nextTimestamp() ) );
				return true;
			}
			else {
				return false;
			}
		}
		finally {
			writeLock.unlock();
		}
	}


	@Override
	public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {
		try {
			writeLock.lock();
			Lockable item = (Lockable) getInternalRegion().get( session, key );

			if ( item != null && item.isUnlockable( lock ) ) {
				Lock lockItem = (Lock) item;
				if ( lockItem.wasLockedConcurrently() ) {
					decrementLock( session, key, lockItem );
					return false;
				}
				else {
					getInternalRegion().put( session, key, new Item( value, null, getInternalRegion().getRegionFactory().nextTimestamp() ) );
					return true;
				}
			}
			else {
				handleLockExpiry( session, key, item );
				return false;
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	Comparator getVersionComparator() {
		return metadata.getVersionComparator();
	}

	@Override
	protected boolean isDefaultMinimalPutOverride() {
		return getInternalRegion().getRegionFactory().getSettings().isMinimalPutsEnabled();
	}

	@Override
	public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {
		return getCacheKeysFactory().createNaturalIdKey( naturalIdValues, persister, session );
	}

	@Override
	public Object[] getNaturalIdValues(Object cacheKey) {
		return getCacheKeysFactory().getNaturalIdValues( cacheKey );
	}
}
