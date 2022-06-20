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
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.ValueBinder;

import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_LOGGER;
import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.MUTATION_EXEC_TRACE_ENABLED;

/**
 * @author Steve Ebersole
 */
public class SingleTableGroupedParameterBinder implements ParameterBinderImplementor {
	private final PreparedStatementGroup statementGroup;
	private final BindingGroup bindingGroup;

	public SingleTableGroupedParameterBinder(PreparedStatementGroup statementGroup) {
		if ( MUTATION_EXEC_TRACE_ENABLED ) {
			MUTATION_EXEC_LOGGER.trace( "Using grouped parameter binding for mutation execution" );
		}

		this.statementGroup = statementGroup;
		this.bindingGroup = new BindingGroup( statementGroup.getSqlGroup().getSingleTableMutation().getTableName() );
	}

	@Override
	public void bindParameter(
			Object value,
			ValueBinder<Object> valueBinder,
			int position,
			String tableName,
			SharedSessionContractImplementor session) {
		assert tableName.equals( bindingGroup.getTableName() );
		bindingGroup.bindParameter(
				value,
				valueBinder,
				position
		);
	}

	@Override
	public boolean beforeStatement(String tableName, SharedSessionContractImplementor session) {
		assert tableName.equals( bindingGroup.getTableName() );
		final PreparedStatementDetails statementDetails = statementGroup.getPreparedStatementDetails( tableName );
		if ( statementDetails == null ) {
			return false;
		}

		bindingGroup.forEachBinding( (binding) -> {
			try {
				binding.getValueBinder().bind(
						statementDetails.getStatement(),
						binding.getValue(),
						binding.getPosition(),
						session
				);
			}
			catch (SQLException e) {
				throw session.getJdbcServices().getSqlExceptionHelper().convert(
						e,
						String.format(
								Locale.ROOT,
								"Unable to bind parameter #%s - %s",
								binding.getPosition(),
								binding.getValue()
						)
				);
			}
		} );

		bindingGroup.clear();

		return true;
	}

}
