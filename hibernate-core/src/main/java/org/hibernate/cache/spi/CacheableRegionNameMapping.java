/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

import java.util.List;

/**
 * Mapping for a particular region-name to the user's caching definitions
 * for the entities, collections and natural-ids to be stored in that region.
 * <p/>
 * Designed under the principle that each kind of "cacheable" can
 * define only one AccessType and that there is only one definition
 * for entity hierarchies that must be defined under the root entity name.
 *
 * @author Steve Ebersole
 */
public interface CacheableRegionNameMapping {
	String getRegionName();

	List<RequestedEntityCaching> getRequestedEntityCachingList();
	List<RequestedCollectionCaching> getRequestedCollectionCachingList();
	List<RequestedNaturalIdCaching> getRequestedNaturalIdCachingList();
}
