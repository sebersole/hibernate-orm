/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal;

import java.util.Map;
import java.util.function.Supplier;

import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.mutation.MutationExecutor;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.engine.jdbc.mutation.internal.batched.BatchedExecutor;
import org.hibernate.engine.jdbc.mutation.internal.batched.BatchedSingleTableExecutor;
import org.hibernate.engine.jdbc.mutation.internal.post.PostInsertExecutor;
import org.hibernate.engine.jdbc.mutation.internal.unbatched.UnBatchedExecutor;
import org.hibernate.engine.jdbc.mutation.internal.unbatched.UnBatchedSingleTableExecutor;
import org.hibernate.engine.jdbc.mutation.spi.MutationExecutorService;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.TableMutation;

/**
 * @author Steve Ebersole
 */
public class StandardMutationExecutorService implements MutationExecutorService {
	private final int globalBatchSize;

	public StandardMutationExecutorService(Map<String, Object> configurationValues, ServiceRegistryImplementor registry) {
		globalBatchSize = ConfigurationHelper.getInt( Environment.STATEMENT_BATCH_SIZE, configurationValues, 1 );
	}

	@Override
	public MutationExecutor createExecutor(
			MutationType mutationType,
			MutationTarget mutationTarget,
			Supplier<BatchKey> batchKeySupplier,
			Supplier<MutationSqlGroup<? extends TableMutation>> sqlGroupSupplier,
			SharedSessionContractImplementor session) {
		// specialized handling for inserts using post-insert id generation
		if ( mutationType == MutationType.INSERT
				&& mutationTarget.getIdentityInsertStatementExecutor() != null ) {
			return new PostInsertExecutor( mutationTarget, sqlGroupSupplier.get(), session );
		}

		// decide whether to us batching - any number > zero means to batch
		final Integer sessionBatchSize = session.getJdbcCoordinator()
				.getJdbcSessionOwner()
				.getJdbcBatchSize();
		final int batchSizeToUse = sessionBatchSize == null
				? globalBatchSize
				: sessionBatchSize;
		if ( batchSizeToUse > 1 ) {
			if ( mutationTarget.getNumberOfTables() == 1 ) {
				return new BatchedSingleTableExecutor( batchKeySupplier.get(), mutationType, mutationTarget, batchSizeToUse, sqlGroupSupplier, session );
			}
			else {
				return new BatchedExecutor( batchKeySupplier.get(), mutationType, mutationTarget, batchSizeToUse, sqlGroupSupplier, session );
			}
		}

		// fallback to simple, non-batched execution
		if ( mutationTarget.getNumberOfTables() == 1 ) {
			return new UnBatchedSingleTableExecutor( mutationType, mutationTarget, sqlGroupSupplier.get(), session );
		}

		return new UnBatchedExecutor( mutationType, mutationTarget, sqlGroupSupplier.get(), session );
	}
}
