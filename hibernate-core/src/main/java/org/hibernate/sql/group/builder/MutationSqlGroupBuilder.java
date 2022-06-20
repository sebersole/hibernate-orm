/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.hibernate.Incubating;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.SingleTableMutationSqlGroup;
import org.hibernate.sql.group.StandardMutationSqlGroup;
import org.hibernate.sql.group.TableMutation;

/**
 * Builder for {@link StandardMutationSqlGroup} instances
 *
 * @see StandardTableInsertBuilder
 * @see StandardTableUpdateBuilder
 * @see StandardTableDeleteBuilder
 *
 * @author Steve Ebersole
 */
@Incubating
public class MutationSqlGroupBuilder<B extends TableMutationBuilder<? extends TableMutation>> {
	private final MutationType mutationType;
	private final MutationTarget mutationTarget;
	private final Map<String, B> tableMutationBuilderMap;
	private B lastBuilder;

	public MutationSqlGroupBuilder(MutationType mutationType, MutationTarget mutationTarget) {
		this.mutationType = mutationType;
		this.mutationTarget = mutationTarget;
		this.tableMutationBuilderMap = new LinkedHashMap<>();
	}

	public B findTableDetailsBuilder(String name) {
		return tableMutationBuilderMap.get( name );
	}

	public B getTableDetailsBuilder(String name) {
		final B builder = findTableDetailsBuilder( name );
		if ( builder == null ) {
			throw new RuntimeException( "Expecting already existing TableMutationBuilder : " + name );
		}
		return builder;
	}

	public void addTableDetailsBuilder(B builder) {
		tableMutationBuilderMap.put( builder.getTableName(), builder );
		lastBuilder = builder;
	}

	public void forEachTableMutationBuilder(Consumer<B> consumer) {
		tableMutationBuilderMap.forEach( (name, mutationBuilder) -> consumer.accept( mutationBuilder ) );
	}

	public MutationSqlGroup<TableMutation> buildGroup() {
		if ( tableMutationBuilderMap.size() == 1 ) {
			return new SingleTableMutationSqlGroup<>( mutationType, mutationTarget, lastBuilder.createMutation() );
		}

		final ArrayList<TableMutation> tableMutations = new ArrayList<>( tableMutationBuilderMap.size() );
		tableMutationBuilderMap.forEach( (name, tableDetailsBuilder) -> {
			final TableMutation tableMutation = tableDetailsBuilder.createMutation();
			if ( tableMutation != null ) {
				tableMutations.add( tableMutation );
			}
		} );

		return new StandardMutationSqlGroup<>( mutationType, mutationTarget, tableMutations );
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"MutationSqlGroupBuilder( %s:`%s` )",
				mutationType.name(),
				mutationTarget.getNavigableRole().getFullPath()
		);
	}

}
