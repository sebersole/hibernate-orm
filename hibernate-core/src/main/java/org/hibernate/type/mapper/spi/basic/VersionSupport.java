/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Contract for managing version values as used for optimistic locking.
 *
 * @author Steve Ebersole
 */
public interface VersionSupport<T> {
	/**
	 * Generate an initial version.
	 *
	 * @param session The session from which this request originates.
	 *
	 * @return an instance of the type
	 */
	T seed(SharedSessionContractImplementor session);

	/**
	 * Increment the version.
	 *
	 * @param session The session from which this request originates.
	 * @param current the current version
	 *
	 * @return an instance of the type
	 */
	T next(T current, SharedSessionContractImplementor session);
}
