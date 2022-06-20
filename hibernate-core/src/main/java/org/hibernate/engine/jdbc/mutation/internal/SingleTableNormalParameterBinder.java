/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal;

import java.sql.SQLException;
import java.util.Locale;

import org.hibernate.engine.jdbc.group.PreparedStatementDetails;
import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.group.SingleTablePreparedStatementGroup;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.ValueBinder;

import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_LOGGER;
import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_TRACE_ENABLED;

/**
 * @author Steve Ebersole
 */
public class SingleTableNormalParameterBinder implements ParameterBinderImplementor {
	private final SingleTablePreparedStatementGroup statementGroup;

	public SingleTableNormalParameterBinder(PreparedStatementGroup statementGroup) {
		assert statementGroup instanceof SingleTablePreparedStatementGroup;

		if ( MUTATION_EXEC_TRACE_ENABLED ) {
			MUTATION_EXEC_LOGGER.trace( "Using single-table as-is parameter binding for mutation execution" );
		}

		this.statementGroup = (SingleTablePreparedStatementGroup) statementGroup;
	}

	@Override
	public void bindParameter(
			Object value,
			ValueBinder<Object> valueBinder,
			int position,
			String tableName,
			SharedSessionContractImplementor session) {
		final PreparedStatementDetails statementDetails = statementGroup.getStatementDetails();
		try {
			valueBinder.bind( statementDetails.getStatement(), value, position, session );
		}
		catch (SQLException e) {
			throw session.getJdbcServices().getSqlExceptionHelper().convert(
					e,
					String.format(
							Locale.ROOT,
							"Unable to bind parameter #%s - %s",
							position,
							value
					)
			);
		}
	}

	@Override
	public boolean beforeStatement(String tableName, SharedSessionContractImplementor session) {
		// nothing to do
		return true;
	}
}
