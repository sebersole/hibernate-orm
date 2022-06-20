/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal.batched;

import java.sql.PreparedStatement;
import java.util.Locale;
import java.util.function.Supplier;

import org.hibernate.engine.jdbc.batch.spi.Batch2;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.group.StandardPreparedStatementGroup;
import org.hibernate.engine.jdbc.mutation.MutationExecutor;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.engine.jdbc.mutation.ParameterBinder;
import org.hibernate.engine.jdbc.mutation.internal.Helper;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.TableMutation;

import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_LOGGER;
import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_TRACE_ENABLED;

/**
 * MutationExecutor implementation based on JDBC statement batching
 *
 * @see PreparedStatement#addBatch
 * @see PreparedStatement#executeBatch
 *
 * @author Steve Ebersole
 */
public class BatchedExecutor implements MutationExecutor {
	private final Batch2 batch;
	private final ParameterBinderImplementor parameterBinder;

	public BatchedExecutor(
			BatchKey batchKey,
			MutationType mutationType,
			MutationTarget mutationTarget,
			Integer batchSizeToUse,
			Supplier<MutationSqlGroup<? extends TableMutation>> sqlGroupSupplier,
			SharedSessionContractImplementor session) {
		assert batchSizeToUse > 1;

		if ( MUTATION_EXEC_TRACE_ENABLED ) {
			MUTATION_EXEC_LOGGER.tracef(
					"Using batched executor for `%` - %s; batch-size = %s",
					mutationTarget.getNavigableRole().getFullPath(),
					mutationType.name(),
					batchSizeToUse
			);
		}

		final MutationSqlGroup<? extends TableMutation> sqlGroup = sqlGroupSupplier.get();
		// NOTE: `tablesToSkip` are the tables for this "row"

		this.batch = session.getJdbcCoordinator().getBatch2(
				batchKey,
				batchSizeToUse,
				// because the PreparedStatementGroup is cached on the Batch, we cannot use
				// the row-specific checker within the PreparedStatementGroup itself.
				// we instead apply the skip handling to the parameter-binder and to the
				// `#execute` -> `Batch#addToBatch` handling
				() -> new StandardPreparedStatementGroup( sqlGroup, session )
		);

		// todo (6.x) : we could also look at whether there are any parameter bindings for the table
		//		inside the Batch and skip if not.
		// 		- see `#execute`

		// unlike PreparedStatementGroup, we maintain a unique parameter-binder
		// per execution
		this.parameterBinder = Helper.resolveParameterBinder( getStatementGroup() );
	}

	@Override
	public PreparedStatementGroup getStatementGroup() {
		return batch.getStatementGroup();
	}

	@Override
	public ParameterBinder getParameterBinder() {
		return parameterBinder;
	}

	@Override
	public void execute(SharedSessionContractImplementor session) {
		// NOTE : we do process the parameter-binder directly here
		// in order to avoid iterating the statements twice -
		// once here to process the binds (for grouped) and then
		// again in addBatch
		batch.addToBatch( parameterBinder );
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"BatchedExecutor(`%s`)",
				batch.getKey().toString()
		);
	}
}
