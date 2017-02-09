/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.NaturalIdCacheDataDescription;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Eric Dalquist
 */
class ReadOnlyNaturalIdRegionAccess extends BaseNaturalIdRegionAccess {
	ReadOnlyNaturalIdRegionAccess(
			NaturalIdCacheDataDescription metadata,
			CacheKeysFactory cacheKeysFactory,
			RegionImpl region) {
		super( metadata, cacheKeysFactory, region );
	}

	@Override
	public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {
		evict( key );
	}
}
