/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.spi;

import java.util.function.Supplier;

import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.mutation.MutationExecutor;
import org.hibernate.engine.jdbc.mutation.MutationTarget;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.service.Service;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.TableMutation;

/**
 * @author Steve Ebersole
 */
public interface MutationExecutorService extends Service {
	MutationExecutor createExecutor(
			MutationType mutationType,
			MutationTarget mutationTarget,
			Supplier<BatchKey> batchKeySupplier,
			Supplier<MutationSqlGroup<? extends TableMutation>> sqlGroupSupplier,
			SharedSessionContractImplementor session);
}
