/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import java.util.Set;

import org.hibernate.sql.group.TableMutation;

/**
 * Contract for builders of {@link TableMutation} builders
 * @author Steve Ebersole
 */
public interface TableMutationBuilder<M extends TableMutation> {
	/**
	 * The name of the table for which a mutation is being built
	 */
	String getTableName();

	int getPrimaryTableIndex();

	Set<Integer> getTableIndexes();

	void addTableIndex(int tableIndex);

	/**
	 * The builder
	 */
	M createMutation();
}
