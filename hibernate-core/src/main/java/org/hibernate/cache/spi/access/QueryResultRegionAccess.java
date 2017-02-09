/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi.access;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.QueryKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.spi.Type;

/**
 * Models access to query-result data.
 *
 * @author Steve Ebersole
 */
public interface QueryResultRegionAccess extends NonUserModelRegionAccess {
	/**
	 * Clear items from the query cache.
	 *
	 * @throws CacheException Indicates a problem delegating to the underlying cache.
	 */
	void clear() throws CacheException;

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
			QueryKey key,
			Type[] returnTypes,
			boolean isNaturalKeyLookup,
			Set<Serializable> spaces,
			SharedSessionContractImplementor session) throws HibernateException;

	/**
	 * Destroy the cache.
	 */
	void destroy();
}
