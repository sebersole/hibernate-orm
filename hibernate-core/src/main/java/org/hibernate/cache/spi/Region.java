/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cache.spi;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccess;
import org.hibernate.cache.spi.access.EntityRegionAccess;
import org.hibernate.cache.spi.access.NaturalIdRegionAccess;
import org.hibernate.cache.spi.access.QueryResultRegionAccess;
import org.hibernate.cache.spi.access.UpdateTimestampsRegionAccess;

/**
 * Defines a contract for accessing a particular named region within the 
 * underlying cache implementation.
 * <p/>
 * Implementations are free to not support the mixing of "user model data"
 * (such as {@link #buildEntityRegionAccess}, {@link #buildNaturalIdRegionAccess},
 * {@link #buildCollectionAccess}) and "support data" (such as
 * {@link #buildQueryResultRegionAccess} and {@link #buildUpdateTimestampsRegionAccess})
 * within the same region.
 * <p/>
 * <b>However</b>, implementors <b>must</b> support mixing "user model data" access
 * from the same region
 *
 * @author Steve Ebersole
 */
public interface Region {
	// todo (6.0) : and what about the other 6.0-todo entry about cache entry structuring, etc.  Does that have any bearing/impact here?

	/**
	 * Retrieve the name of this region.
	 *
	 * @return The region name
	 */
	String getName();

	/**
	 * The "end state" contract of the region's lifecycle.  Called
	 * during {@link org.hibernate.SessionFactory#close()} to give
	 * the region a chance to cleanup.
	 *
	 * @throws org.hibernate.cache.CacheException Indicates problem shutting down
	 */
	void destroy() throws CacheException;

	/**
	 * Build a EntityRegionAccess instance representing access to entity data stored in
	 * this cache region using the given AccessType.
	 *
	 * @param accessType The type of access allowed to the cached data, in terms of
	 * concurrency and consistency controls.
	 * @param metadata Information about the entity we are storing/accessing data for
	 *
	 * @return The access delegate
	 */
	EntityRegionAccess buildEntityRegionAccess(
			AccessType accessType,
			EntityCacheDataDescription metadata) throws CacheException;

	/**
	 * Build a NaturalIdRegionAccess instance representing access to natural-id
	 * data stored in this cache region using the given AccessType.
	 *
	 * @param accessType The type of access allowed to the cached data, in terms of
	 * concurrency and consistency controls.
	 * @param metadata Information about the natural-id we are storing/accessing data for
	 *
	 * @return The access delegate
	 */
	NaturalIdRegionAccess buildNaturalIdRegionAccess(
			AccessType accessType,
			NaturalIdCacheDataDescription metadata) throws CacheException;

	/**
	 * Build a CollectionRegionAccess instance representing access to collection
	 * data stored in this cache region using the given AccessType.
	 *
	 * @param accessType The type of access allowed to the cached data, in terms of
	 * concurrency and consistency controls.
	 * @param metadata Information about the collection we are storing/accessing data for
	 *
	 * @return The access delegate
	 */
	CollectionRegionAccess buildCollectionAccess(
			AccessType accessType,
			CollectionCacheDataDescription metadata) throws CacheException;

	QueryResultRegionAccess buildQueryResultRegionAccess();

	UpdateTimestampsRegionAccess buildUpdateTimestampsRegionAccess();
}
