/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.Map;

/**
 * Specialization for {@linkplain TableMutation table mutations} which define
 * a restriction for the mutation - UPDATE and DELETE.
 *
 * @author Steve Ebersole
 */
public interface RestrictedTableMutation extends TableMutation {
	/**
	 * Get the complete mapping of parameter indexes by column-name
	 */
	Map<String, Integer> getRestrictedColumnParamIndexMap();

	/**
	 * Get the parameter index for a particular column-name, returning {@code null}
	 * if not (yet) known
	 *
	 * @see #getRestrictedColumnParamIndexMap
	 */
	Integer findRestrictedColumnParamIndex(String columnName);

	/**
	 * Get the parameter index for a particular column-name, throwing an exception
	 * if not (yet) known
	 *
	 * @see #getRestrictedColumnParamIndexMap
	 */
	Integer getRestrictedColumnParamIndex(String columnName);
}
