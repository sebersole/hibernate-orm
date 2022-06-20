/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.engine.jdbc.batch.internal;

import java.util.Locale;

import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.hibernate.engine.jdbc.batch.spi.Batch2;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;

/**
 * Common code across BatchBuilder service implementors
 */
final class SharedBatchBuildingCode {

	static Batch buildBatch(final int defaultJdbcBatchSize, final BatchKey key, final JdbcCoordinator jdbcCoordinator) {
		final Integer sessionJdbcBatchSize = jdbcCoordinator.getJdbcSessionOwner()
				.getJdbcBatchSize();
		final int jdbcBatchSizeToUse = sessionJdbcBatchSize == null ?
				defaultJdbcBatchSize :
				sessionJdbcBatchSize;
		return jdbcBatchSizeToUse > 1
				? new BatchingBatch( key, jdbcCoordinator, jdbcBatchSizeToUse )
				: new NonBatchingBatch( key, jdbcCoordinator );
	}

	public static Batch2 buildBatch(
			int defaultBatchSize,
			BatchKey key,
			PreparedStatementGroup preparedStatementGroup,
			JdbcCoordinator jdbcCoordinator) {
		final Integer sessionBatchSize = jdbcCoordinator.getJdbcSessionOwner().getJdbcBatchSize();
		final int batchSizeToUse = sessionBatchSize == null
				? defaultBatchSize
				: sessionBatchSize;

		if ( batchSizeToUse > 1 ) {
			return new Batch2Impl( key, preparedStatementGroup, batchSizeToUse, jdbcCoordinator );
		}

		throw new UnsupportedOperationException(
				String.format(
						Locale.ROOT,
						"Cannot build batch with batch-size == %s; should be greater than 1",
						batchSizeToUse
				)
		);
	}
}
