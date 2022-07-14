/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal.unbatched;

import java.sql.SQLException;
import java.util.Locale;

import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.group.SingleTablePreparedStatementGroup;
import org.hibernate.engine.jdbc.mutation.MutationExecutor;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.engine.jdbc.mutation.ParameterBinder;
import org.hibernate.engine.jdbc.mutation.internal.Helper;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.TableMutation;

import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_LOGGER;
import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_TRACE_ENABLED;

/**
 * MutationExecutor when not using JDBC batching
 *
 * @author Steve Ebersole
 */
public class UnBatchedSingleTableExecutor implements MutationExecutor {
	private final MutationType mutationType;
	private final MutationTarget mutationTarget;

	private final SingleTablePreparedStatementGroup statementGroup;
	private final ParameterBinderImplementor parameterBinder;

	public UnBatchedSingleTableExecutor(
			MutationType mutationType,
			MutationTarget mutationTarget,
			MutationSqlGroup<? extends TableMutation> sqlGroup,
			SharedSessionContractImplementor session) {
		if ( MUTATION_EXEC_TRACE_ENABLED ) {
			MUTATION_EXEC_LOGGER.tracef(
					"Using non-batched executor for `%` - %s",
					mutationTarget.getNavigableRole().getFullPath(),
					mutationType.name()
			);
		}

		this.mutationType = mutationType;
		this.mutationTarget = mutationTarget;

		assert sqlGroup.getNumberOfTableMutations() == 1;
		this.statementGroup = new SingleTablePreparedStatementGroup( sqlGroup, session );
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
	public Object execute(Object modelReference, SharedSessionContractImplementor session) {
		final SqlStatementLogger sqlStatementLogger = session.getJdbcServices().getSqlStatementLogger();
		try {
			statementGroup.forEachStatement( (tableName, statementDetails) -> {
				sqlStatementLogger.logStatement( statementDetails.getTableMutation().getSqlString() );
				parameterBinder.beforeStatement( tableName, session );

				try {
					final int rowCount = session.getJdbcCoordinator().getResultSetReturn().executeUpdate( statementDetails.getStatement() );

					statementDetails.getExpectation().verifyOutcome(
							rowCount,
							statementDetails.getStatement(),
							-1,
							statementDetails.getTableMutation().getSqlString()
					);
				}
				catch (SQLException e) {
					throw session.getJdbcServices().getSqlExceptionHelper().convert(
							e,
							"Unable to execute mutation PreparedStatement against table `" + tableName + "`",
							statementDetails.getTableMutation().getSqlString()
					);
				}
			} );
		}
		finally {
			statementGroup.release();
		}

		return null;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"UnBatchedExecutor( %s:`%s`)",
				mutationType.name(),
				mutationTarget.getNavigableRole().getFullPath()
		);
	}
}
