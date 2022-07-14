/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.hibernate.HibernateException;
import org.hibernate.Incubating;
import org.hibernate.engine.jdbc.mutation.MutationTarget;

/**
 * Groups together the SQL mutation statements for all tables
 * related to an entity (joined tables, secondary tables, etc)
 *
 * @author Steve Ebersole
 */
@Incubating
public class StandardMutationSqlGroup<M extends TableMutation> implements MutationSqlGroup<M> {
	private final MutationType mutationType;
	private final MutationTarget mutationTarget;
	private final List<M> tableMutationList;

	public StandardMutationSqlGroup(MutationType mutationType, MutationTarget mutationTarget, ArrayList<M> tableMutationList) {
		this.mutationType = mutationType;
		this.mutationTarget = mutationTarget;
		this.tableMutationList = tableMutationList;
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
		return tableMutationList.size();
	}

	@Override
	public M getSingleTableMutation() {
		if ( tableMutationList.size() > 1 ) {
			throw new HibernateException( "MutationSqlGroup contained more than one table mutation" );
		}
		return tableMutationList.iterator().next();
	}

	@Override
	public M getTableMutation(String tableName) {
		for ( int i = 0; i < tableMutationList.size(); i++ ) {
			final M tableMutation = tableMutationList.get( i );
			if ( tableMutation != null ) {
				if ( tableMutation.getTableName().equals( tableName ) ) {
					return tableMutation;
				}
			}
		}
		return null;
	}

	@Override
	public M getTableMutation(int position) {
		for ( int i = 0; i < tableMutationList.size(); i++ ) {
			final M tableMutation = tableMutationList.get( i );
			if ( tableMutation.getPrimaryTableIndex() == position ) {
				return tableMutation;
			}

			if ( tableMutation.getTableIndexes().contains( position ) ) {
				return tableMutation;
			}
		}

		throw new IllegalArgumentException( "Could not locate TableMutation #" + position );
	}

	@Override
	public void forEachTableMutation(BiConsumer<Integer, M> action) {
		for ( int i = 0; i < tableMutationList.size(); i++ ) {
			action.accept( i, tableMutationList.get( i ) );
		}
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
