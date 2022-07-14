/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal.post;

import java.util.Locale;

import org.hibernate.engine.jdbc.group.PreparedStatementDetails;
import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.group.StandardPreparedStatementGroup;
import org.hibernate.engine.jdbc.mutation.MutationExecutor;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.engine.jdbc.mutation.ParameterBinder;
import org.hibernate.engine.jdbc.mutation.internal.Helper;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.TableMutation;

import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_LOGGER;
import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_TRACE_ENABLED;

/**
 * @author Steve Ebersole
 */
public class PostInsertSingleTableExecutor implements MutationExecutor {
	private final MutationTarget mutationTarget;
	private final StandardPreparedStatementGroup statementGroup;
	private final ParameterBinderImplementor parameterBinder;

	public PostInsertSingleTableExecutor(
			MutationTarget mutationTarget,
			MutationSqlGroup<? extends TableMutation> sqlGroup,
			SharedSessionContractImplementor session) {
		assert mutationTarget.getIdentityInsertDelegate() != null;

		if ( MUTATION_EXEC_TRACE_ENABLED ) {
			MUTATION_EXEC_LOGGER.tracef(
					"Using post-insert executor for `%s`",
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
	public Object execute(Object modelReference, SharedSessionContractImplementor session) {
		final InsertGeneratedIdentifierDelegate identityHandler = mutationTarget.getIdentityInsertDelegate();
		final PreparedStatementDetails idTableStatementDetails = statementGroup.getPreparedStatementDetails( mutationTarget.getIdentifierTableName() );

		final Object id = identityHandler.performInsert( idTableStatementDetails, parameterBinder, modelReference, session );

		if ( MUTATION_EXEC_TRACE_ENABLED ) {
			MUTATION_EXEC_LOGGER.tracef(
					"Post-insert generated value : `%s` (%s)",
					id,
					mutationTarget.getNavigableRole().getFullPath()
			);
		}

		// todo (6.2) : apply id to non-identifier tables

		return id;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"PostInsertSingleTableExecutor(`%s`)",
				mutationTarget.getNavigableRole().getFullPath()
		);
	}
}
