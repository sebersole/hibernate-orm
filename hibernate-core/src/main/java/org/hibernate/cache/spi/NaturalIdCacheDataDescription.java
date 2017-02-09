/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

/**
 * Specialized CacheDataDescription for natural-id data.
 *
 * @author Steve Ebersole
 */
public interface NaturalIdCacheDataDescription extends CacheDataDescription {
	/**
	 * The name of the entity this natural-id belongs to.
	 */
	String getEntityName();
}
