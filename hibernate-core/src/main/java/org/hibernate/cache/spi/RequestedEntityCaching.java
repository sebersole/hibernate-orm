/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi;

import org.hibernate.persister.common.NavigableRole;

/**
 * @author Steve Ebersole
 */
public interface RequestedEntityCaching extends RequestedCaching {
	NavigableRole getRootEntityNavigableRole();

	default String getRootEntityName() {
		return getRootEntityNavigableRole().getFullPath();
	}
}
