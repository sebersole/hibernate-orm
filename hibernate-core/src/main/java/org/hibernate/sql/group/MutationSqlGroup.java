/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.function.BiConsumer;

import org.hibernate.engine.jdbc.mutation.MutationTarget;

/**
 * @author Steve Ebersole
 */
public interface MutationSqlGroup<M extends TableMutation> {
	MutationType getMutationType();

	MutationTarget getMutationTarget();

	int getNumberOfTableMutations();

	M getSingleTableMutation();

	M getTableMutation(String tableName);

	M getTableMutation(int position);

	void forEachTableMutation(BiConsumer<Integer, M> action);
}
