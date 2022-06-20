/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import org.hibernate.sql.group.TableUpdate;

/**
 * @author Steve Ebersole
 */
public interface TableUpdateBuilder extends ValuedTableMutationBuilder<TableUpdate>, RestrictedTableMutationBuilder<TableUpdate> {
	void setVersionColumn(String columnName);

	default void addRestrictionColumn(String columnName) {
		addRestrictionColumn( columnName, "?" );
	}

	void addRestrictionColumn(String columnName, String valueExpression);

	void setWhere(String fragment);

	default void addPrimaryKeyColumn(String columnName) {
		addRestrictionColumn( columnName );
	}
}
