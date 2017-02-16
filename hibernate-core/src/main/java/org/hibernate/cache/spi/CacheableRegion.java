/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionStorageAccess;
import org.hibernate.cache.spi.access.EntityStorageAccess;
import org.hibernate.cache.spi.access.NaturalIdStorageAccess;
import org.hibernate.persister.common.NavigableRole;

/**
 * A Region for "cacheable" (entity, collection, natural-id) data
 *
 * @author Steve Ebersole
 */
public interface CacheableRegion extends Region {
	/**
	 * Build a EntityRegionAccess instance representing access to entity data
	 * stored in this cache region using the given AccessType.
	 *
	 * @param rootEntityRole The root entity name for the hierarchy whose data
	 * we want to access
	 */
	EntityStorageAccess getEntityStorageAccess(NavigableRole rootEntityRole) throws CacheException;

	/**
	 * Build a NaturalIdRegionAccess instance representing access to natural-id
	 * data stored in this cache region using the given AccessType.
	 *
	 * @param rootEntityRole The NavigableRole of the root entity whose
	 * natural-id data we want to access
	 */
	NaturalIdStorageAccess getNaturalIdStorageAccess(NavigableRole rootEntityRole) throws CacheException;

	/**
	 * Build a CollectionRegionAccess instance representing access to collection
	 * data stored in this cache region using the given AccessType.
	 *
	 * @param collectionRole The NavigableRole of the collection whose data
	 * we want to access
	 */
	CollectionStorageAccess getCollectionStorageAccess(NavigableRole collectionRole) throws CacheException;
}
