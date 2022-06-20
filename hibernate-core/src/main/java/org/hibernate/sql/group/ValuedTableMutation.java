/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.Map;

/**
 * Specialization for {@linkplain TableMutation table mutations} which apply values
 * to a row of the table - INSERT and UPDATE.
 *
 * @apiNote Access to parameter indexes are kept separate between
 * key and non-key columns, respectively, via:<ul>
 *     <li>{@link #getValuesKeyColumnParamIndexMap}</li>
 *     <li>{@link #getValuesColumnParamIndexMap}</li>
 * </ul>
 *
 * @author Steve Ebersole
 */
public interface ValuedTableMutation extends TableMutation {
	/**
	 * Get the complete mapping of parameter indexes by non-key column-name
	 * in the mutation's values list
	 */
	Map<String, Integer> getValuesColumnParamIndexMap();

	/**
	 * The position of the parameter for a particular non-key column in the
	 * mutation's values list, returning {@code null} if not (yet) known
	 *
	 * @see #getValuesColumnParamIndexMap
	 */
	Integer findValuesColumnParamIndex(String columnName);

	/**
	 * The position of the parameter for a particular non-key column in the
	 * mutation's values list, throwing an exception if not (yet) known
	 *
	 * @see #getValuesColumnParamIndexMap
	 */
	Integer getValuesColumnParamIndex(String columnName);

	/**
	 * Get the complete mapping of parameter indexes by key column-name
	 * in the mutation's values list
	 */
	Map<String, Integer> getValuesKeyColumnParamIndexMap();

	/**
	 * The position of the parameter for a particular key column in the
	 * mutation's values list, returning {@code null} if not (yet) known
	 *
	 * @see #getValuesKeyColumnParamIndexMap
	 */
	Integer findValuesKeyColumnParamIndex(String columnName);

	/**
	 * The position of the parameter for a particular key column in the
	 * mutation's values list, throwing an exception if not (yet) known
	 *
	 * @see #getValuesKeyColumnParamIndexMap
	 */
	Integer getValuesKeyColumnParamIndex(String columnName);
}
