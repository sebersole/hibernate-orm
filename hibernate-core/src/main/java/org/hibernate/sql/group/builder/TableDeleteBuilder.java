/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import org.hibernate.sql.group.TableDelete;

/**
 * @apiNote The internal processing relies on the following order of calls
 * for handling the restrictions to properly handle parameter ordering:<ol>
 *     <li>{@link #addPrimaryKeyColumn}</li>
 *     <li>{@link #setVersionColumn}</li>
 * </ol>

 * @author Steve Ebersole
 */
public interface TableDeleteBuilder extends TableMutationBuilder<TableDelete> {
	void addPrimaryKeyColumn(String columnName);

	void setVersionColumn(String columnName);

	void setWhere(String fragment);

	void addWhereFragment(String fragment);

	@Override
	TableDelete createMutation();
}
