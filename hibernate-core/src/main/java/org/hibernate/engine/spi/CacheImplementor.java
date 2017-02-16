/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.engine.spi;

import java.io.Serializable;

import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.QueryResultsCache;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.UpdateTimestampsRegion;
import org.hibernate.cache.spi.access.CollectionStorageAccess;
import org.hibernate.cache.spi.access.EntityStorageAccess;
import org.hibernate.cache.spi.access.NaturalIdStorageAccess;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.Service;

/**
 * Define internal contact of <tt>Cache API</tt>
 *
 * @author Strong Liu
 */
public interface CacheImplementor extends Service, Cache, Serializable {

	/**
	 * Close all cache regions.
	 */
	void close();

	/**
	 * The underlying RegionFactory in use.
	 *
	 * @return The {@code RegionFactory}
	 */
	RegionFactory getRegionFactory();

	QueryResultsCache getQueryResultsCache();

	/**
	 * Get {@code UpdateTimestampsCache} instance managed by the {@code SessionFactory}.
	 */
	UpdateTimestampsRegion getUpdateTimestampsRegion();

	/**
	 * Clean up the default {@code QueryCache}.
	 *
 	 * @throws HibernateException
	 */
	default void evictQueries() throws HibernateException {
		getQueryResultsCache().evictAll();
	}

	/**
	 * Applies any defined prefix, handling all {@code null} checks.
	 *
	 * @param regionName The region name to qualify
	 *
	 * @return The qualified name
	 */
	String qualifyRegionName(String regionName);

	/**
	 * Get the names of <tt>all</tt> cache regions, including entity, collection, natural-id and query caches.
	 *
	 * @return All cache region names
	 */
	String[] getSecondLevelCacheRegionNames();

	/**
	 * Find the "access strategy" for the named entity cache region.
	 *
	 * @param regionName The name of the region
	 *
	 * @return That region's "access strategy"
	 */
	EntityStorageAccess getEntityRegionAccess(String regionName);

	/**
	 * Find the "access strategy" for the named collection cache region.
	 *
	 * @param regionName The name of the region
	 *
	 * @return That region's "access strategy"
	 */
	CollectionStorageAccess getCollectionRegionAccess(String regionName);

	/**
	 * Find the "access strategy" for the named natrual-id cache region.
	 *
	 * @param regionName The name of the region
	 *
	 * @return That region's "access strategy"
	 */
	NaturalIdStorageAccess getNaturalIdCacheRegionAccessStrategy(String regionName);

	EntityStorageAccess determineEntityRegionAccessStrategy(PersistentClass model);

	NaturalIdStorageAccess determineNaturalIdRegionAccessStrategy(PersistentClass model);

	CollectionStorageAccess determineCollectionRegionAccessStrategy(Collection model);
}
