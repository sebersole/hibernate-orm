/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Represents a value that can ultimately be written to the database.  The process of
 * getting a value ready to write to the database involves any number of steps:
 *
 * 		* For basic values, this usually only applies any "value converters" (attribute converters, enum conversions, etc)
 *		* For components, this is (1) splits the composite into the individual sub-values array and then (2)
 *			applies any value conversions on these...
 *
 * @author Steve Ebersole
 */
public interface Writeable<D,I,R> {
	/**
	 * Produce a multi-dimensional array of extracted simple value
	 */
	I unresolve(D value, SharedSessionContractImplementor session);

	/**
	 * Produce a flattened array from dehydrated state
	 */
	R dehydrate(I values, SharedSessionContractImplementor session);
}
