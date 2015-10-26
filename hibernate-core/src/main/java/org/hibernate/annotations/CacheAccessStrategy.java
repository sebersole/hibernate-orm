/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.annotations;

import org.hibernate.cache.spi.access.AccessType;

/**
 * Possible values for {@link Cache#access()} to indicate the access strategy for caching
 *
 * @author Steve Ebersole
 */
public enum CacheAccessStrategy {
	/**
	 * The selected cache provider's default access strategy will be used.
	 */
	UNSPECIFIED( null ),
	/**
	 * {@link AccessType#READ_ONLY}
	 */
	READ_ONLY( AccessType.READ_ONLY ),
	/**
	 * {@link AccessType#READ_WRITE}
	 */
	READ_WRITE( AccessType.READ_WRITE ),
	/**
	 * {@link AccessType#NONSTRICT_READ_WRITE}
	 */
	NONSTRICT_READ_WRITE( AccessType.NONSTRICT_READ_WRITE ),
	/**
	 * {@link AccessType#TRANSACTIONAL}
	 */
	TRANSACTIONAL( AccessType.TRANSACTIONAL );

	private final AccessType accessType;

	CacheAccessStrategy(AccessType accessType) {
		this.accessType = accessType;
	}

	public AccessType getCorrespondingAccessType() {
		return accessType;
	}
}
