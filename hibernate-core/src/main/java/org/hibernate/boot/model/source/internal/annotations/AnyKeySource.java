/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;

import org.hibernate.boot.model.naming.ImplicitAnyKeyColumnNameSource;

/**
 * Describes the key column of an ANY mapping (the id of the corresponding entity)
 *
 * @author Steve Ebersole
 */
public interface AnyKeySource extends ImplicitAnyKeyColumnNameSource {
	/**
	 * Retrieve information about the Hibernate Type describing the key.
	 *
	 * @return The key information
	 */
	HibernateTypeSource getTypeSource();

	/**
	 * Retrieve information about column(s) holding the key value.
	 *
	 * @return The column(s) information
	 */
	List<RelationalValueSource> getRelationalValueSources();
}
