/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.group;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.BiConsumer;

import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.MutationStatementPreparer;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.resource.jdbc.ResourceRegistry;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.SingleTableMutationSqlGroup;
import org.hibernate.sql.group.TableMutation;

/**
 * @author Steve Ebersole
 */
public class SingleTablePreparedStatementGroup implements PreparedStatementGroup {
	private final SingleTableMutationSqlGroup<?> sqlGroup;
	private final SharedSessionContractImplementor session;

	private PreparedStatementDetails statementDetails;

	public SingleTablePreparedStatementGroup(MutationSqlGroup<?> sqlGroup, SharedSessionContractImplementor session) {
		this.sqlGroup = (SingleTableMutationSqlGroup<?>) sqlGroup;
		this.session = session;
	}

	public PreparedStatementDetails getStatementDetails() {
		return statementDetails;
	}

	@Override
	public int getNumberOfStatements() {
		return statementDetails == null ? 0 : 1;
	}

	@Override
	public void forEachStatement(BiConsumer<String, PreparedStatementDetails> action) {
		if ( statementDetails != null ) {
			action.accept( statementDetails.getTableMutation().getTableName(), statementDetails );
		}
	}

	@Override
	public PreparedStatementDetails getPreparedStatementDetails(String tableName) {
		if ( statementDetails != null ) {
			return statementDetails;
		}

		final TableMutation tableMutation = sqlGroup.getTableMutation( tableName );
		if ( tableMutation == null ) {
			// should indicate the table has been skipped
			return null;
		}

		// todo (6.2) : figure out a better way to involve InsertGeneratedIdentifierDelegate
		final PreparedStatement statement;
		if ( sqlGroup.getMutationType() == MutationType.INSERT
				&& sqlGroup.getMutationTarget().getIdentityInsertDelegate() != null ) {
			statement = sqlGroup.getMutationTarget().getIdentityInsertDelegate().prepareStatement(
					tableMutation.getSqlString(),
					session
			);
		}
		else {
			final JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();
			final MutationStatementPreparer statementPreparer = jdbcCoordinator.getMutationStatementPreparer();
			statement = statementPreparer.prepareStatement( tableMutation.getSqlString(), tableMutation.isCallable() );
		}

		try {
			statementDetails = new StandardPreparedStatementDetails( tableMutation, statement, tableMutation.getExpectation() );
			return statementDetails;
		}
		catch (SQLException e) {
			throw session.getJdbcServices().getSqlExceptionHelper().convert(
					e,
					"Error preparing PreparedStatement",
					tableMutation.getSqlString()
			);
		}
	}

	@Override
	public void release() {
		if ( statementDetails != null ) {
			final ResourceRegistry resourceRegistry = session.getJdbcCoordinator().getLogicalConnection().getResourceRegistry();
			resourceRegistry.release( statementDetails.getStatement() );

			statementDetails = null;
		}
	}

	@Override
	public MutationSqlGroup<? extends TableMutation> getSqlGroup() {
		return sqlGroup;
	}
}
