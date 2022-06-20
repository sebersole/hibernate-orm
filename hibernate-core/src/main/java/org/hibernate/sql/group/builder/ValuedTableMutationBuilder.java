/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import org.hibernate.sql.group.ValuedTableMutation;

/**
 * TableMutationBuilder specialization for building ValuedTableMutation references
 *
 * @author Steve Ebersole
 */
public interface ValuedTableMutationBuilder<M extends ValuedTableMutation> extends TableMutationBuilder<M> {
	/**
	 * Add a non-key column as part of the values list
	 */
	default void addValuesColumn(String columnName) {
		addValuesColumn( columnName, "?" );
	}

	/**
	 * Add a non-key column as part of the values list using the specified
	 * value expression.
	 */
	void addValuesColumn(String columnName, String valueExpression);

	/**
	 * Add a non-key LOB-valued column as part of the values list
	 */
	void addValuesLobColumn(String selectionExpression, String valueExpression);

	/**
	 * Add a key column as part of the values list
	 */
	default void addValuesKeyColumn(String columnName) {
		addValuesKeyColumn( columnName, "?" );
	}

	/**
	 * Add a key column as part of the values list using the specified
	 * 	 * value expression.
	 */
	void addValuesKeyColumn(String columnName, String valueExpression);

	@Override
	M createMutation();
}
