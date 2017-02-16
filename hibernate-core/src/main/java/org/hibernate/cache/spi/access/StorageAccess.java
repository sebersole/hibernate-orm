/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cache.spi.access;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheableRegion;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.common.NavigableRole;

/**
 * Base contract for accessing an underlying storage for a cacheable in a transactionally
 * ACID manner.
 *
 * @author Steve Ebersole
 * @author Gail Badner
 */
public interface StorageAccess {
	/**
	 * The CacheableRegion this access comes from.
	 */
	CacheableRegion getRegion();

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
	/**
	 * Get the {@link NavigableRole} of the Navigable whose data is being
	 * accessed.
	 */
	NavigableRole getAccessedNavigableRole();

	/**
	 * Attempt to retrieve an object from the cache. Mainly used in attempting
	 * to resolve entities/collections from the second level cache.
	 * <p/>
	 * Note, this method used to accept {@code long txTimestamp}.  But that information
	 * is already available via {@link SharedSessionContractImplementor#getTransactionStartTimestamp()}
	 *
	 * @param session Current session.
	 * @param key The key of the item to be retrieved.
	 *
	 * @return the cached object or <tt>null</tt>
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	Object get(SharedSessionContractImplementor session, Object key) throws CacheException;

	/**
	 * Attempt to cache an object, afterQuery loading from the database.
	 * <p/>
	 * Note, this method used to accept {@code long txTimestamp}.  But that information
	 * is already available via {@link SharedSessionContractImplementor#getTransactionStartTimestamp()}
	 *
	 * @param session Current session.
	 * @param key The item key
	 * @param value The item
	 * @param version the item version number
	 *
	 * @return <tt>true</tt> if the object was successfully cached
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	boolean putFromLoad(
			SharedSessionContractImplementor session,
			Object key,
			Object value,
			Object version) throws CacheException;

	/**
	 * Attempt to cache an object, afterQuery loading from the database, explicitly
	 * specifying the minimalPut behavior.
	 * <p/>
	 * Note, this method used to accept {@code long txTimestamp}.  But that information
	 * is already available via {@link SharedSessionContractImplementor#getTransactionStartTimestamp()}
	 *
	 * @param session Current session.
	 * @param key The item key
	 * @param value The item
	 * @param version the item version number
	 * @param minimalPutOverride Explicit minimalPut flag
	 *
	 * @return <tt>true</tt> if the object was successfully cached
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	boolean putFromLoad(
			SharedSessionContractImplementor session,
			Object key,
			Object value,
			Object version,
			boolean minimalPutOverride) throws CacheException;

	/**
	 * We are going to attempt to update/delete the keyed object. This
	 * method is used by "asynchronous" concurrency strategies.
	 * <p/>
	 * The returned object must be passed back to {@link #unlockItem}, to release the
	 * lock. Concurrency strategies which do not support client-visible
	 * locks may silently return null.
	 *
	 * @param session Current session.
	 * @param key The key of the item to lock
	 * @param version The item's current version value
	 *
	 * @return A representation of our lock on the item; or null.
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException;

	/**
	 * Lock the entire region
	 *
	 * @return A representation of our lock on the item; or null.
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	SoftLock lockRegion() throws CacheException;

	/**
	 * Called when we have finished the attempted update/delete (which may or
	 * may not have been successful), afterQuery transaction completion.  This method
	 * is used by "asynchronous" concurrency strategies.
	 *
	 * @param session Current session.
	 * @param key The item key
	 * @param lock The lock previously obtained from {@link #lockItem}
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException;

	/**
	 * Called afterQuery we have finished the attempted invalidation of the entire
	 * region
	 *
	 * @param lock The lock previously obtained from {@link #lockRegion}
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	void unlockRegion(SoftLock lock) throws CacheException;

	/**
	 * Called afterQuery an item has become stale (beforeQuery the transaction completes).
	 * This method is used by "synchronous" concurrency strategies.
	 *
	 * @param session
	 * @param key The key of the item to remove
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	void remove(SharedSessionContractImplementor session, Object key) throws CacheException;

	/**
	 * Called to evict data from the entire region
	 *
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	void removeAll() throws CacheException;
}
