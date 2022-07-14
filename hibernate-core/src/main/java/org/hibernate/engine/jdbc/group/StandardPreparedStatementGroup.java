/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.group;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import org.hibernate.Incubating;
import org.hibernate.Internal;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.MutationStatementPreparer;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.resource.jdbc.ResourceRegistry;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.MutationType;
import org.hibernate.sql.group.TableMutation;

/**
 * A group of {@link StandardPreparedStatementDetails} references related to multi-table
 * entity mappings.  The statements are keyed by each table-names.
 *
 * @author Steve Ebersole
 */
@Incubating
public class StandardPreparedStatementGroup implements PreparedStatementGroup {
	private final MutationSqlGroup<? extends TableMutation> sqlGroup;
	private final SharedSessionContractImplementor session;

	private final SortedMap<String, PreparedStatementDetails> statementMap;

	public StandardPreparedStatementGroup(MutationSqlGroup<? extends TableMutation> sqlGroup, SharedSessionContractImplementor session) {
		this.sqlGroup = sqlGroup;
		this.session = session;

		this.statementMap = new TreeMap<>(
				Comparator.comparingInt(
						(tableName) -> sqlGroup.getTableMutation( tableName ).getPrimaryTableIndex()
				)
		);
	}

	@Override
	@Internal
	public MutationSqlGroup<? extends TableMutation> getSqlGroup() {
		return sqlGroup;
	}

	@Override
	public int getNumberOfStatements() {
		return statementMap.size();
	}

	@Override
	public void forEachStatement(BiConsumer<String, PreparedStatementDetails> action) {
		statementMap.forEach( action );
	}

	@Override
	public PreparedStatementDetails getPreparedStatementDetails(String tableName) {
		final PreparedStatementDetails existingStatementDetails = statementMap.get( tableName );
		if ( existingStatementDetails != null ) {
			return existingStatementDetails;
		}

		final JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();
		final MutationStatementPreparer statementPreparer = jdbcCoordinator.getMutationStatementPreparer();

		final TableMutation tableMutation = sqlGroup.getTableMutation( tableName );
		if ( tableMutation == null ) {
			// should indicate the table has been skipped
			return null;
		}

		final PreparedStatement statement;
		if ( sqlGroup.getMutationType() == MutationType.INSERT
				&& sqlGroup.getMutationTarget().getIdentityInsertDelegate() != null
				&& tableMutation.getTableName().equals( sqlGroup.getMutationTarget().getIdentifierTableName() ) ) {
			statement = sqlGroup.getMutationTarget().getIdentityInsertDelegate().prepareStatement(
					tableMutation.getSqlString(),
					session
			);
		}
		else {
			statement = statementPreparer.prepareStatement( tableMutation.getSqlString(), tableMutation.isCallable() );
		}

		try {
			final PreparedStatementDetails statementDetails = new StandardPreparedStatementDetails( tableMutation, statement, tableMutation.getExpectation() );
			statementMap.put( tableMutation.getTableName(), statementDetails );
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
		final ResourceRegistry resourceRegistry = session.getJdbcCoordinator().getLogicalConnection().getResourceRegistry();

		//noinspection CodeBlock2Expr
		statementMap.forEach( (tableName, statementDetails) -> {
			resourceRegistry.release( statementDetails.getStatement() );
		} );

		statementMap.clear();
	}
}
