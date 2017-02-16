/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

import java.util.Comparator;

import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.persister.common.NavigableRole;
import org.hibernate.type.spi.Type;

/**
 * The "caching config" requested for a "cacheable".
 * <p/>
 * What all a "cacheable" might be is manifested in the
 * specializations of this contract.
 *
 * @author Steve Ebersole
 */
public interface RequestedCaching {
	/**
	 * The NavigableRole for which the described caching is requested.
	 */
	NavigableRole getCachedRole();

	/**
	 * The AccessType requested for the role.
	 */
	AccessType getAccessType();

	/**
	 * Whether or not the data to be cached is mutable.
	 */
	boolean isMutable();

	/**
	 * Whether or not the data to be cached here is versioned (
	 * defines optimistic locking)
	 */
	boolean isVersioned();

	/**
	 * The Comparator used to compare two different version values.  May return {@code null} <b>if</b>
	 * {@link #isVersioned()} returns {@code false}.
	 */
	Comparator getVersionComparator();

	/**
	 * The Type of the cached data's key.
	 */
	Type getKeyType();
}
