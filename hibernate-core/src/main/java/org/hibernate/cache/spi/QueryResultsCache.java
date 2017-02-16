/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.cache.CacheException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.service.spi.Stoppable;
import org.hibernate.type.Type;

/**
 * Manages caching query results as well as recognizing stale results.
 *
 * @author Steve Ebersole
 */
public interface QueryResultsCache extends Stoppable {
	/**
	 * Clear items from the query cache.
	 *
	 * @throws CacheException Indicates a problem delegating to the underlying cache.
	 */
	void clear(String region) throws CacheException;

	/**
	 * Put a result into the query cache.
	 *
	 * @param key The cache key
	 * @param returnTypes The result types
	 * @param result The results to cache
	 * @param isNaturalKeyLookup Was this a natural id lookup?
	 * @param session The originating session
	 *
	 * @return Whether the put actually happened.
	 *
	 * @throws HibernateException Indicates a problem delegating to the underlying cache.
	 */
	boolean put(
			String regionName,
			QueryKey key,
			Type[] returnTypes,
			List result,
			boolean isNaturalKeyLookup,
			SharedSessionContractImplementor session) throws HibernateException;

	/**
	 * Get results from the cache.
	 *
	 * @param key The cache key
	 * @param returnTypes The result types
	 * @param isNaturalKeyLookup Was this a natural id lookup?
	 * @param spaces The query spaces (used in invalidation plus validation checks)
	 * @param session The originating session
	 *
	 * @return The cached results; may be null.
	 *
	 * @throws HibernateException Indicates a problem delegating to the underlying cache.
	 */
	List get(
			String regionName,
			QueryKey key,
			Type[] returnTypes,
			boolean isNaturalKeyLookup,
			Set<Serializable> spaces,
			SharedSessionContractImplementor session) throws HibernateException;

	/**
	 * Destroy the cache.
	 *
	 * @deprecated (6.0) Use {@link #stop} instead
	 */
	@Deprecated
	void destroy();

	@Override
	default void stop() {
		destroy();
	}
}
