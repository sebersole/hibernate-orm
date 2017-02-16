/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.RequestedEntityCaching;
import org.hibernate.cache.spi.access.EntityStorageAccess;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.spi.EntityPersister;

/**
 * @author Strong Liu
 */
public class ReadWriteEntityRegionAccess
		extends AbstractReadWriteAccess
		implements EntityStorageAccess {

	ReadWriteEntityRegionAccess(
			RequestedEntityCaching metadata,
			CacheKeysFactory cacheKeysFactory,
			CacheableRegionImpl region) {
		super( region, metadata, cacheKeysFactory );
	}

	@Override
	public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {
		return putFromLoad( session, key, value, version, isDefaultMinimalPutOverride() );
	}

	@Override
	public boolean insert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {
		return false;
	}

	@Override
	public boolean update(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)
			throws CacheException {
		return false;
	}

	@Override
	public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {

		try {
			writeLock.lock();
			Lockable item = (Lockable) getInternalRegion().get( session, key );
			if ( item == null ) {
				getInternalRegion().put( session, key, new Item( value, version, getInternalRegion().getRegionFactory().nextTimestamp() ) );
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
	public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)
			throws CacheException {
		try {
			writeLock.lock();
			Lockable item = (Lockable) getInternalRegion().get( session, key );

			if ( item != null && item.isUnlockable( lock ) ) {
				Lock lockItem = (Lock) item;
				if ( lockItem.wasLockedConcurrently() ) {
					decrementLock(session, key, lockItem );
					return false;
				}
				else {
					getInternalRegion().put( session, key, new Item( value, currentVersion, getInternalRegion().getRegionFactory().nextTimestamp() ) );
					return true;
				}
			}
			else {
				handleLockExpiry(session, key, item );
				return false;
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	protected boolean isDefaultMinimalPutOverride() {
		return getInternalRegion().getRegionFactory().getSettings().isMinimalPutsEnabled();
	}

	@Override
	public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
		return getCacheKeysFactory().createEntityKey( id, persister, factory, tenantIdentifier );
	}

	@Override
	public Object getCacheKeyId(Object cacheKey) {
		return getCacheKeysFactory().getEntityId( cacheKey );
	}
}
