/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.RequestedCollectionCaching;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Strong Liu
 */
class NonstrictReadWriteCollectionRegionAccess extends BaseCollectionRegionAccess {
	NonstrictReadWriteCollectionRegionAccess(
			RequestedCollectionCaching metadata,
			CacheKeysFactory cacheKeysFactory,
			CacheableRegionImpl region) {
		super( cacheKeysFactory, region, metadata.getCachedRole() );
	}

	@Override
	public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {
		evict( key );
	}

	@Override
	public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {
		evict( key );
	}

	@Override
	public void removeAll() throws CacheException {
		evictAll();
	}
}
