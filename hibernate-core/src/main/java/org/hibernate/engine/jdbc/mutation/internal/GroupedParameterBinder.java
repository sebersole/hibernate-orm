/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
public class GroupedParameterBinder implements ParameterBinderImplementor {
	private final PreparedStatementGroup statementGroup;
	private final Map<String, BindingGroup> bindingGroupMap = new HashMap<>();

	public GroupedParameterBinder(PreparedStatementGroup statementGroup) {
		if ( MUTATION_EXEC_TRACE_ENABLED ) {
			MUTATION_EXEC_LOGGER.trace( "Using grouped parameter binding for mutation execution" );
		}

		this.statementGroup = statementGroup;
	}

	@Override
	public void bindParameter(
			Object value,
			ValueBinder<Object> valueBinder,
			int position,
			String tableName,
			SharedSessionContractImplementor session) {
		resolveBindingGroup( tableName ).bindParameter(
				value,
				valueBinder,
				position
		);
	}

	private BindingGroup resolveBindingGroup(String tableName) {
		final BindingGroup existing = bindingGroupMap.get( tableName );
		if ( existing != null ) {
			assert tableName.equals( existing.getTableName() );
			return existing;
		}

		final BindingGroup created = new BindingGroup( tableName );
		bindingGroupMap.put( tableName, created );
		return created;
	}

	@Override
	public boolean beforeStatement(String tableName, SharedSessionContractImplementor session) {
		final PreparedStatementDetails statementDetails = statementGroup.getPreparedStatementDetails( tableName );
		if ( statementDetails == null ) {
			return false;
		}

		final BindingGroup bindingGroup = bindingGroupMap.get( tableName );
		if ( bindingGroup == null ) {
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
		bindingGroupMap.remove( tableName );

		return true;
	}

}
