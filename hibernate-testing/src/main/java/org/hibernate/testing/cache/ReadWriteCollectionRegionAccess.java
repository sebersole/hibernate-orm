/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.RequestedCollectionCaching;
import org.hibernate.cache.spi.access.CollectionStorageAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.spi.CollectionPersister;

/**
 * @author Strong Liu
 */
class ReadWriteCollectionRegionAccess extends AbstractReadWriteAccess implements CollectionStorageAccess {
	public ReadWriteCollectionRegionAccess(
			RequestedCollectionCaching requestedCollectionCaching,
			CacheKeysFactory factoryToUse,
			CacheableRegionImpl region) {
		super( region, requestedCollectionCaching, factoryToUse );
	}

	@Override
	protected boolean isDefaultMinimalPutOverride() {
		return getInternalRegion().getRegionFactory().getSettings().isMinimalPutsEnabled();
	}

	@Override
	public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
		return getCacheKeysFactory().createCollectionKey( id, persister, factory, tenantIdentifier );
	}

	@Override
	public Object getCacheKeyId(Object cacheKey) {
		return getCacheKeysFactory().getCollectionId( cacheKey );
	}
}
