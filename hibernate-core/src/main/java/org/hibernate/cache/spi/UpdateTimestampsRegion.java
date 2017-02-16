/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

import java.io.Serializable;
import java.util.Set;

import org.hibernate.cache.CacheException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Steve Ebersole
 */
public interface UpdateTimestampsRegion extends Region {
	@Override
	default String getName() {
		return UpdateTimestampsRegion.class.getName();
	}

	/**
	 * Perform pre-invalidation.
	 *
	 * @param spaces The spaces to pre-invalidate
	 * @param session The session whether this invalidation originated.
	 *
	 * @throws CacheException Indicated problem delegating to underlying region.
	 */
	void preInvalidate(Serializable[] spaces, SharedSessionContractImplementor session) throws CacheException;

	/**
	 * Perform invalidation.
	 *
	 * @param spaces The spaces to invalidate
	 * @param session The session whether this invalidation originated.
	 *
	 * @throws CacheException Indicated problem delegating to underlying region.
	 */
	void invalidate(Serializable[] spaces, SharedSessionContractImplementor session) throws CacheException;

	/**
	 * Perform an up-to-date check for the given set of query spaces as part of verifying
	 * the validity of cached query results.
	 *
	 * @param spaces The spaces to check
	 * @param timestamp The timestamp against which to check, which kept with the cached results
	 * @param session The session whether this check originated.
	 *
	 * @return Whether all those spaces are up-to-date
	 *
	 * @throws CacheException Indicated problem delegating to underlying region.
	 */
	boolean isUpToDate(Set<Serializable> spaces, Long timestamp, SharedSessionContractImplementor session) throws CacheException;
}
