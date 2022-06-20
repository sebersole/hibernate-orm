/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.group.SingleTablePreparedStatementGroup;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.internal.util.collections.LazySet;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.TableMutation;
import org.hibernate.type.descriptor.JdbcBindingLogging;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static ParameterBinderImplementor resolveParameterBinder(PreparedStatementGroup statementGroup) {
		final boolean useGrouping = JdbcBindingLogging.TRACE_ENABLED;
		if ( statementGroup instanceof SingleTablePreparedStatementGroup ) {
			return useGrouping
					? new SingleTableGroupedParameterBinder( statementGroup )
					: new SingleTableNormalParameterBinder( statementGroup );
		}
		else {
			return useGrouping
					? new GroupedParameterBinder( statementGroup )
					: new NormalParameterBinder( statementGroup );
		}
	}

	public static Set<TableMutation> determineTablesToSkip(
			MutationType mutationType,
			MutationTarget mutationTarget,
			MutationSqlGroup<? extends TableMutation> sqlGroup,
			Function<TableMutation,Boolean> skipChecker) {
		if ( mutationType.canSkipTables() && mutationTarget.hasSkippableTables() ) {
			if ( sqlGroup.getNumberOfTableMutations() == 0 ) {
				return Collections.emptySet();
			}

			if ( sqlGroup.getNumberOfTableMutations() == 1 ) {
				final TableMutation tableMutation = sqlGroup.getSingleTableMutation();
				if ( tableMutation.getPrimaryTableIndex() != 0 && skipChecker.apply( tableMutation ) ) {
					return Collections.singleton( tableMutation );
				}
			}

			final LazySet<TableMutation> tablesToSkipCollector = new LazySet<>();
			sqlGroup.forEachTableMutation( (position, tableMutation) -> {
				if ( tableMutation.getPrimaryTableIndex() != 0 && skipChecker.apply( tableMutation ) ) {
					tablesToSkipCollector.add( tableMutation );
				}
			} );
			return tablesToSkipCollector.getUnderlyingSet();
		}

		return Collections.emptySet();
	}

	private Helper() {
		// disallow direct instantiation
	}
}
