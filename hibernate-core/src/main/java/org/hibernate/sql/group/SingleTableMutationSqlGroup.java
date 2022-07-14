/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.Locale;
import java.util.function.BiConsumer;

import org.hibernate.Incubating;
import org.hibernate.engine.jdbc.mutation.MutationTarget;

/**
 * Groups together the SQL mutation statements for all tables
 * related to an entity (joined tables, secondary tables, etc)
 *
 * @author Steve Ebersole
 */
@Incubating
public class SingleTableMutationSqlGroup<M extends TableMutation> implements MutationSqlGroup<M> {
	private final MutationType mutationType;
	private final MutationTarget mutationTarget;
	private final M tableMutation;

	public SingleTableMutationSqlGroup(MutationType mutationType, MutationTarget mutationTarget, M tableMutation) {
		this.mutationType = mutationType;
		this.mutationTarget = mutationTarget;
		this.tableMutation = tableMutation;
	}

	@Override
	public MutationType getMutationType() {
		return mutationType;
	}

	@Override
	public MutationTarget getMutationTarget() {
		return mutationTarget;
	}

	@Override
	public int getNumberOfTableMutations() {
		return 1;
	}

	@Override
	public M getSingleTableMutation() {
		return tableMutation;
	}

	@Override
	public M getTableMutation(String tableName) {
		assert tableMutation.getTableName().equals( tableName );
		return tableMutation;
	}

	@Override
	public M getTableMutation(int position) {
		if ( position != 0 ) {
			throw new IllegalArgumentException( "A SingleTableMutationSqlGroup contains only a single table" );
		}
		return tableMutation;
	}

	@Override
	public void forEachTableMutation(BiConsumer<Integer, M> action) {
		action.accept( 0, tableMutation );
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"MutationSqlGroup( %s:`%s` )",
				mutationType.name(),
				mutationTarget.getNavigableRole().getFullPath()
		);
	}
}
