/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.internal;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.spi.NonSelectQueryPlan;
import org.hibernate.query.spi.ParameterBindingContext;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.sqm.consume.multitable.spi.DeleteHandler;
import org.hibernate.query.sqm.consume.multitable.spi.HandlerExecutionContext;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;

/**
 * @author Steve Ebersole
 */
public class MultiTableDeleteQueryPlan implements NonSelectQueryPlan {
	private final QuerySqmImpl query;
	private final DeleteHandler deleteHandler;

	public MultiTableDeleteQueryPlan(
			QuerySqmImpl query,
			DeleteHandler deleteHandler) {
		this.query = query;
		this.deleteHandler = deleteHandler;
	}

	@Override
	public int executeUpdate(
			SharedSessionContractImplementor session,
			QueryOptions queryOptions,
			JdbcParameterBindings jdbcParameterBindings,
			ParameterBindingContext parameterBindingContext) {
		return deleteHandler.execute(
				new HandlerExecutionContext() {
					@Override
					public SharedSessionContractImplementor getSession() {
						return session;
					}

					@Override
					public ExecutionContext getExecutionContext() {
						return query;
					}
				},
				parameterBindingContext
		);
	}
}
