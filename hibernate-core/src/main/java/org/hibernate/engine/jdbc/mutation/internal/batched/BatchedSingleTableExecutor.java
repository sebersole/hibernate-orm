/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal.batched;

import java.util.function.Supplier;

import org.hibernate.engine.jdbc.batch.spi.Batch2;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.group.SingleTablePreparedStatementGroup;
import org.hibernate.engine.jdbc.mutation.MutationExecutor;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.engine.jdbc.mutation.ParameterBinder;
import org.hibernate.engine.jdbc.mutation.internal.Helper;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.SingleTableMutationSqlGroup;
import org.hibernate.sql.group.TableMutation;

import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_LOGGER;
import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_TRACE_ENABLED;

/**
 * @author Steve Ebersole
 */
public class BatchedSingleTableExecutor implements MutationExecutor {
	private final ParameterBinderImplementor parameterBinder;
	private final Batch2 batch;

	public BatchedSingleTableExecutor(
			BatchKey batchKey,
			MutationType mutationType,
			MutationTarget mutationTarget,
			int batchSizeToUse,
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
		assert sqlGroup instanceof SingleTableMutationSqlGroup;

		this.batch = session.getJdbcCoordinator().getBatch2(
				batchKey,
				batchSizeToUse,
				// because the PreparedStatementGroup is cached on the Batch, we cannot use
				// the row-specific checker within the PreparedStatementGroup itself.
				// we instead apply the skip handling to the parameter-binder and to the
				// `#execute` -> `Batch#addToBatch` handling
				() -> new SingleTablePreparedStatementGroup( sqlGroup, session )
		);

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
		batch.addToBatch( parameterBinder );
	}
}
