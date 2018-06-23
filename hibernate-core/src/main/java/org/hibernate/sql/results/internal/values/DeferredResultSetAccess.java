/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.values;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreLogging;
import org.hibernate.resource.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.exec.spi.PreparedStatementCreator;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class DeferredResultSetAccess extends AbstractResultSetAccess {
	private static final Logger log = CoreLogging.logger( DeferredResultSetAccess.class );

	private final JdbcSelect jdbcSelect;
	private final ExecutionContext executionContext;
	private final JdbcParameterBindings jdbcParameterBindings;
	private final PreparedStatementCreator statementCreator;

	private PreparedStatement preparedStatement;
	private ResultSet resultSet;

	public DeferredResultSetAccess(
			JdbcSelect jdbcSelect,
			ExecutionContext executionContext,
			JdbcParameterBindings jdbcParameterBindings,
			PreparedStatementCreator statementCreator) {
		super( executionContext.getSession() );
		this.executionContext = executionContext;
		this.jdbcSelect = jdbcSelect;
		this.jdbcParameterBindings = jdbcParameterBindings;
		this.statementCreator = statementCreator;
	}

	@Override
	public ResultSet getResultSet() {
		if ( resultSet == null ) {
			executeQuery();
		}
		return resultSet;
	}

	@Override
	public SessionFactoryImplementor getFactory() {
		return executionContext.getSession().getFactory();
	}

	private void executeQuery() {
		final LogicalConnectionImplementor logicalConnection = getPersistenceContext().getJdbcCoordinator().getLogicalConnection();
		final Connection connection = logicalConnection.getPhysicalConnection();

		final JdbcServices jdbcServices = getPersistenceContext().getFactory().getServiceRegistry().getService( JdbcServices.class );

		final String sql = jdbcSelect.getSql();

		try {
			log.tracef( "Executing query to retrieve ResultSet : %s", sql );
			jdbcServices.getSqlStatementLogger().logStatement( sql );

			// prepare the query
			preparedStatement = statementCreator.create( connection, sql );
			logicalConnection.getResourceRegistry().register( preparedStatement, true );

			// set options
			if ( executionContext.getQueryOptions().getFetchSize() != null ) {
				preparedStatement.setFetchSize( executionContext.getQueryOptions().getFetchSize() );
			}
			if ( executionContext.getQueryOptions().getTimeout() != null ) {
				preparedStatement.setQueryTimeout( executionContext.getQueryOptions().getTimeout() );
			}

			// todo : limit/offset

			// todo : validate that all query parameters were bound?

			// todo (6.0) (domain-jdbc) : another blurring of the lines between domain and jdbc
			//		Here specifically, the jdbc binder has access to the domain bindings - how does
			//		it get the jdbc value(s)?

			int paramBindingPosition = 1;

			for ( ParameterSpec parameterSpec : jdbcSelect.getJdbcParameters() ) {
				final Object bindValue = jdbcParameterBindings.getBindValue( parameterSpec );
				parameterSpec.getJdbcValueMapper().getJdbcValueBinder().bind(
						preparedStatement,
						bindValue,
						paramBindingPosition++,
						executionContext
				);
			}

			resultSet = preparedStatement.executeQuery();
			logicalConnection.getResourceRegistry().register( resultSet, preparedStatement );

		}
		catch (SQLException e) {
			throw jdbcServices.getSqlExceptionHelper().convert(
					e,
					"JDBC exception executing SQL [" + sql + "]"
			);
		}
		finally {
			logicalConnection.afterStatement();
		}
	}

	@Override
	public void release() {
		if ( resultSet != null ) {
			getPersistenceContext().getJdbcCoordinator()
					.getLogicalConnection()
					.getResourceRegistry()
					.release( resultSet, preparedStatement );
			resultSet = null;
		}

		if ( preparedStatement != null ) {
			getPersistenceContext().getJdbcCoordinator()
					.getLogicalConnection()
					.getResourceRegistry()
					.release( preparedStatement );
			preparedStatement = null;
		}
	}
}
