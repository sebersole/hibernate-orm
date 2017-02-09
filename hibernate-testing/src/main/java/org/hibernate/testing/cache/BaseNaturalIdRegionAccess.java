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
import org.hibernate.cache.spi.access.NaturalIdRegionAccess;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.spi.EntityPersister;

/**
 * @author Eric Dalquist
 */
class BaseNaturalIdRegionAccess extends BaseRegionAccess implements NaturalIdRegionAccess {
	private final CacheKeysFactory cacheKeysFactory;

	public BaseNaturalIdRegionAccess(
			NaturalIdCacheDataDescription metadata,
			CacheKeysFactory cacheKeysFactory,
			RegionImpl region) {
		super( region );
		this.cacheKeysFactory = cacheKeysFactory;
	}

	@Override
	protected boolean isDefaultMinimalPutOverride() {
		return getInternalRegion().getRegionFactory().getSettings().isMinimalPutsEnabled();
	}

	@Override
	public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		return putFromLoad( session, key, value, 0, null );
	}

	@Override
	public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		return false;
	}

	@Override
	public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
		return putFromLoad( session, key, value, 0, null );
	}

	@Override
	public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {
		return false;
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
