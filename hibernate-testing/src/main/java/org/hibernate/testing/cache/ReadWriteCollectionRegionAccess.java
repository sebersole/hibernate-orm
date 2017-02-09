/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import java.util.Comparator;

import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.CollectionCacheDataDescription;
import org.hibernate.cache.spi.access.CollectionRegionAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.spi.CollectionPersister;

/**
 * @author Strong Liu
 */
class ReadWriteCollectionRegionAccess extends AbstractReadWriteAccess implements CollectionRegionAccess {
	private final CollectionCacheDataDescription cacheDataDescription;

	ReadWriteCollectionRegionAccess(
			CollectionCacheDataDescription cacheDataDescription,
			CacheKeysFactory cacheKeysFactory,
			RegionImpl region) {
		super( cacheKeysFactory, region );
		this.cacheDataDescription = cacheDataDescription;
	}

	@Override
	Comparator getVersionComparator() {
		return cacheDataDescription.getVersionComparator();
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
