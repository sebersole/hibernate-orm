/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.spi;

import java.util.List;

/**
 * Information related to domain-query parameters.
 *
 * @author Steve Ebersole
 */
public interface ParameterBindingContext {

	/**
	 * The bindings for any parameters defined by the domain query
	 */
	QueryParameterBindings<?> getQueryParameterBindings();

	/**
	 * Used in entity and collection loaders - the entity id(s) or collection
	 * key(s) to load
	 */
	<T> List<T> getLoadIdentifiers();
}
