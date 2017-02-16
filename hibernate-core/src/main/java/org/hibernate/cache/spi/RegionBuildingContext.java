/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * A "parameter object" for {@link org.hibernate.cache.spi.RegionFactory#buildCacheableRegion}
 * calls, giving it access to information it needs.
 *
 * @author Steve Ebersole
 */
public interface RegionBuildingContext {
	/**
	 * The CacheKeyFactory specifically specified by the configuration of
	 * Hibernate i.e. by the user, by some "container", etc.
	 * <p/>
	 * RegionFactory implementors should use this to be its
	 * CacheKeyFactory when asked later.
	 */
	CacheKeysFactory getEnforcedCacheKeysFactory();

	SessionFactoryImplementor getSessionFactory();
}
