/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal.post;

import java.util.Locale;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.group.StandardPreparedStatementGroup;
import org.hibernate.engine.jdbc.mutation.MutationExecutor;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.engine.jdbc.mutation.ParameterBinder;
import org.hibernate.engine.jdbc.mutation.internal.Helper;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.TableMutation;

import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_LOGGER;
import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_TRACE_ENABLED;

/**
 * @author Steve Ebersole
 */
public class PostInsertExecutor implements MutationExecutor {
	private final MutationTarget mutationTarget;
	private final StandardPreparedStatementGroup statementGroup;
	private final ParameterBinderImplementor parameterBinder;

	public PostInsertExecutor(
			MutationTarget mutationTarget,
			MutationSqlGroup<? extends TableMutation> sqlGroup,
			SharedSessionContractImplementor session) {
		if ( MUTATION_EXEC_TRACE_ENABLED ) {
			MUTATION_EXEC_LOGGER.tracef(
					"Using post-insert executor for `%`",
					mutationTarget.getNavigableRole().getFullPath()
			);
		}

		this.mutationTarget = mutationTarget;

		this.statementGroup = new StandardPreparedStatementGroup( sqlGroup, session );
		this.parameterBinder = Helper.resolveParameterBinder( statementGroup );
	}

	@Override
	public PreparedStatementGroup getStatementGroup() {
		return statementGroup;
	}

	@Override
	public ParameterBinder getParameterBinder() {
		return parameterBinder;
	}

	@Override
	public void execute(SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"PostInsertExecutor(`%s`)",
				mutationTarget.getNavigableRole().getFullPath()
		);
	}
}
