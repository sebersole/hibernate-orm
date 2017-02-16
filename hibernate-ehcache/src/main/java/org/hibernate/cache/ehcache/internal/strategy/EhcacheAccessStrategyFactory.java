/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cache.ehcache.internal.strategy;

import org.hibernate.cache.ehcache.internal.regions.EhcacheCollectionRegion;
import org.hibernate.cache.ehcache.internal.regions.EhcacheEntityRegion;
import org.hibernate.cache.ehcache.internal.regions.EhcacheNaturalIdRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionStorageAccess;
import org.hibernate.cache.spi.access.EntityStorageAccess;
import org.hibernate.cache.spi.access.NaturalIdStorageAccess;
import org.hibernate.cache.spi.access.StorageAccess;

/**
 * Factory to create {@link StorageAccess} instance
 *
 * @author Abhishek Sanoujam
 * @author Alex Snaps
 */
public interface EhcacheAccessStrategyFactory {
	/**
	 * Create {@link EntityStorageAccess} for the input {@link EhcacheEntityRegion} and {@link AccessType}
	 *
	 * @param entityRegion The entity region being wrapped
	 * @param accessType The type of access to allow to the region
	 *
	 * @return the created {@link EntityStorageAccess}
	 */
	public EntityStorageAccess createEntityRegionAccessStrategy(
			EhcacheEntityRegion entityRegion,
			AccessType accessType);

	/**
	 * Create {@link CollectionStorageAccess} for the input {@link EhcacheCollectionRegion} and {@link AccessType}
	 *
	 * @param collectionRegion The collection region being wrapped
	 * @param accessType The type of access to allow to the region
	 *
	 * @return the created {@link CollectionStorageAccess}
	 */
	public CollectionStorageAccess createCollectionRegionAccessStrategy(
			EhcacheCollectionRegion collectionRegion,
			AccessType accessType);

	/**
	 * Create {@link NaturalIdStorageAccess} for the input {@link EhcacheNaturalIdRegion} and {@link AccessType}
	 *
	 * @param naturalIdRegion The natural-id region being wrapped
	 * @param accessType The type of access to allow to the region
	 *
	 * @return the created {@link NaturalIdStorageAccess}
	 */
	public NaturalIdStorageAccess createNaturalIdRegionAccessStrategy(
			EhcacheNaturalIdRegion naturalIdRegion,
			AccessType accessType);

}
