/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.id.insert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.group.PreparedStatementDetails;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.MutationStatementPreparer;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.PostInsertIdentityPersister;

/**
 * Delegate for dealing with IDENTITY columns using JDBC3 getGeneratedKeys
 *
 * @author Andrea Boriero
 */
public class GetGeneratedKeysDelegate extends AbstractReturningDelegate {
	private final PostInsertIdentityPersister persister;
	private final Dialect dialect;

	public GetGeneratedKeysDelegate(PostInsertIdentityPersister persister, Dialect dialect) {
		super( persister );
		this.persister = persister;
		this.dialect = dialect;
	}

	@Override
	public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert(SqlStringGenerationContext context) {
		IdentifierGeneratingInsert insert = new IdentifierGeneratingInsert( dialect );
		insert.addIdentityColumn( persister.getRootTableKeyColumnNames()[0] );
		return insert;
	}

	@Override
	public PreparedStatement prepareStatement(String insertSql, SharedSessionContractImplementor session) {
		final JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();
		final MutationStatementPreparer statementPreparer = jdbcCoordinator.getMutationStatementPreparer();
		return statementPreparer.prepareStatement( insertSql, PreparedStatement.RETURN_GENERATED_KEYS );
	}


	@Override
	public Object performInsert(
			PreparedStatementDetails insertStatementDetails,
			ParameterBinderImplementor parameterBinder,
			Object entity,
			SharedSessionContractImplementor session) {
		final JdbcServices jdbcServices = session.getJdbcServices();
		final JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();

		final String insertSql = insertStatementDetails.getTableMutation().getSqlString();

		jdbcServices.getSqlStatementLogger().logStatement( insertSql );
		parameterBinder.beforeStatement( insertStatementDetails.getTableMutation().getTableName(), session );

		final PreparedStatement insertStatement = insertStatementDetails.getStatement();
		jdbcCoordinator.getResultSetReturn().executeUpdate( insertStatement );

		try {
			final ResultSet rs = insertStatement.getGeneratedKeys();
			try {
				return IdentifierGeneratorHelper.getGeneratedIdentity(
						rs,
						persister.getRootTableKeyColumnNames()[0],
						persister.getIdentifierType(),
						jdbcServices.getJdbcEnvironment().getDialect()
				);
			}
			catch (SQLException e) {
				throw jdbcServices.getSqlExceptionHelper().convert(
						e,
						"Unable to extract generated key(s) from generated-keys ResultSet",
						insertSql
				);
			}
			finally {
				if ( rs != null ) {
					jdbcCoordinator
							.getLogicalConnection()
							.getResourceRegistry()
							.release( rs, insertStatement );
				}
			}
		}
		catch (SQLException e) {
			throw jdbcServices.getSqlExceptionHelper().convert(
					e,
					"Unable to extract generated-keys ResultSet",
					insertSql
			);
		}
	}

	@Override
	public Object executeAndExtract(
			String insertSql,
			PreparedStatement insertStatement,
			SharedSessionContractImplementor session) {
		final JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();
		final JdbcServices jdbcServices = session.getJdbcServices();

		jdbcCoordinator.getResultSetReturn().executeUpdate( insertStatement );

		try {
			final ResultSet rs = insertStatement.getGeneratedKeys();
			try {
				return IdentifierGeneratorHelper.getGeneratedIdentity(
						rs,
						persister.getRootTableKeyColumnNames()[0],
						persister.getIdentifierType(),
						jdbcServices.getJdbcEnvironment().getDialect()
				);
			}
			catch (SQLException e) {
				throw jdbcServices.getSqlExceptionHelper().convert(
						e,
						"Unable to extract generated key(s) from generated-keys ResultSet",
						insertSql
				);
			}
			finally {
				if ( rs != null ) {
					jdbcCoordinator
							.getLogicalConnection()
							.getResourceRegistry()
							.release( rs, insertStatement );
				}
			}
		}
		catch (SQLException e) {
			throw jdbcServices.getSqlExceptionHelper().convert(
					e,
					"Unable to extract generated-keys ResultSet",
					insertSql
			);
		}
	}
}
