/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

import org.hibernate.cache.CacheException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Regions whose data is directly accessed (do not define access through a
 * {@link org.hibernate.cache.spi.access.StorageAccess}).
 *
 * @author Steve Ebersole
 */
public interface DirectAccessRegion extends Region {
	Object get(SharedSessionContractImplementor session, Object key) throws CacheException;
	void put(SharedSessionContractImplementor session, Object key, Object value) throws CacheException;

	/**
	 * Forcibly evict an item from the cache immediately without regard for transaction
	 * isolation.  This behavior is exactly Hibernate legacy behavior, but it is also required
	 * by JPA - so we cannot remove it.
	 *
	 * @param key The key of the item to remove
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	void evict(Object key) throws CacheException;

	/**
	 * Forcibly evict all items from the cache immediately without regard for transaction
	 * isolation.  This behavior is exactly Hibernate legacy behavior, but it is also required
	 * by JPA - so we cannot remove it.
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	void evictAll() throws CacheException;
}
