/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.batch.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashSet;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.batch.spi.Batch2;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.batch.spi.BatchObserver;
import org.hibernate.engine.jdbc.group.PreparedStatementDetails;
import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.resource.jdbc.spi.JdbcObserver;

import org.jboss.logging.Logger;

import static org.hibernate.engine.jdbc.batch.JdbcBatchLogging.BATCH_DEBUG_ENABLED;
import static org.hibernate.engine.jdbc.batch.JdbcBatchLogging.BATCH_LOGGER;
import static org.hibernate.engine.jdbc.batch.JdbcBatchLogging.BATCH_TRACE_ENABLED;

/**
 * Standard implementation of Batch2
 *
 * @author Steve Ebersole
 */
public class Batch2Impl implements Batch2 {
	protected static final CoreMessageLogger LOG = Logger.getMessageLogger(
			CoreMessageLogger.class,
			Batch2Impl.class.getName()
	);

	private final BatchKey key;
	private final int batchSizeToUse;
	private final PreparedStatementGroup statementGroup;

	private final JdbcCoordinator jdbcCoordinator;
	private final SqlStatementLogger sqlStatementLogger;
	private final SqlExceptionHelper sqlExceptionHelper;

	private final LinkedHashSet<BatchObserver> observers = new LinkedHashSet<>();

	private int batchPosition;
	private boolean batchExecuted;

	public Batch2Impl(
			BatchKey key,
			PreparedStatementGroup statementGroup,
			int batchSizeToUse,
			JdbcCoordinator jdbcCoordinator) {
		if ( key == null ) {
			throw new IllegalArgumentException( "Batch key cannot be null" );
		}
		if ( jdbcCoordinator == null ) {
			throw new IllegalArgumentException( "JDBC coordinator cannot be null" );
		}

		this.key = key;
		this.jdbcCoordinator = jdbcCoordinator;
		this.statementGroup = statementGroup;

		final JdbcServices jdbcServices = jdbcCoordinator.getJdbcSessionOwner()
				.getJdbcSessionContext()
				.getServiceRegistry()
				.getService( JdbcServices.class );

		this.sqlStatementLogger = jdbcServices.getSqlStatementLogger();
		this.sqlExceptionHelper = jdbcServices.getSqlExceptionHelper();

		this.batchSizeToUse = batchSizeToUse;

		if ( BATCH_TRACE_ENABLED ) {
			BATCH_LOGGER.tracef(
					"Created Batch2 (%s) - `%s`",
					batchSizeToUse,
					key.toLoggableString()
			);
		}
	}

	@Override
	public final BatchKey getKey() {
		return key;
	}

	@Override
	public PreparedStatementGroup getStatementGroup() {
		return statementGroup;
	}

	@Override
	public void addObserver(BatchObserver observer) {
		observers.add( observer );
	}

	@Override
	public void addToBatch(ParameterBinderImplementor parameterBinder) {
		if ( BATCH_TRACE_ENABLED ) {
			BATCH_LOGGER.tracef(
					"Adding to JDBC batch (%s) - `%s`",
					batchPosition + 1,
					getKey().toLoggableString()
			);
		}

		try {
			getStatementGroup().forEachStatement( (tableName, statementDetails) -> {
				sqlStatementLogger.logStatement( statementDetails.getTableMutation().getSqlString() );
				final boolean handBindings = parameterBinder.beforeStatement(
						tableName,
						(SharedSessionContractImplementor) jdbcCoordinator.getJdbcSessionOwner()
				);
				if ( handBindings ) {
					try {
						statementDetails.getStatement().addBatch();
					}
					catch (SQLException e) {
						LOG.debug( "SQLException escaped proxy", e );
						throw sqlExceptionHelper.convert(
								e,
								"Could not perform addBatch",
								statementDetails.getTableMutation().getSqlString()
						);
					}
				}
			} );
		}
		catch (RuntimeException e) {
			abortBatch( e );
			throw e;
		}

		batchPosition++;
		if ( batchPosition == batchSizeToUse ) {
			notifyObserversImplicitExecution();
			performExecution();
			batchPosition = 0;
			batchExecuted = true;
		}
	}

	protected void releaseStatements() {
		//noinspection CodeBlock2Expr
		getStatementGroup().forEachStatement( (tableName, statementDetails) -> {
			clearBatch( statementDetails.getStatement() );
		} );
		statementGroup.release();
		jdbcCoordinator.afterStatementExecution();
	}

	protected void clearBatch(PreparedStatement statement) {
		try {
			// This code can be called after the connection is released
			// and the statement is closed. If the statement is closed,
			// then SQLException will be thrown when PreparedStatement#clearBatch
			// is called.
			// Ensure the statement is not closed before
			// calling PreparedStatement#clearBatch.
			if ( !statement.isClosed() ) {
				statement.clearBatch();
			}
		}
		catch ( SQLException e ) {
			LOG.unableToReleaseBatchStatement();
		}
	}

	/**
	 * Convenience method to notify registered observers of an explicit execution of this batch.
	 */
	protected final void notifyObserversExplicitExecution() {
		for ( BatchObserver observer : observers ) {
			observer.batchExplicitlyExecuted();
		}
	}

	/**
	 * Convenience method to notify registered observers of an implicit execution of this batch.
	 */
	protected final void notifyObserversImplicitExecution() {
		for ( BatchObserver observer : observers ) {
			observer.batchImplicitlyExecuted();
		}
	}

	protected void abortBatch(Exception cause) {
		try {
			jdbcCoordinator.abortBatch();
		}
		catch (RuntimeException e) {
			cause.addSuppressed( e );
		}
	}

	@Override
	public void execute() {
		notifyObserversExplicitExecution();
		if ( getStatementGroup().getNumberOfStatements() == 0 ) {
			return;
		}

		try {
			if ( batchPosition == 0 ) {
				if( !batchExecuted) {
					if ( BATCH_DEBUG_ENABLED ) {
						BATCH_LOGGER.debugf(
								"No batched statements to execute - %s",
								getKey().toLoggableString()
						);
					}
				}
			}
			else {
				performExecution();
			}
		}
		finally {
			releaseStatements();
		}
	}

	protected void performExecution() {
		if ( BATCH_TRACE_ENABLED ) {
			BATCH_LOGGER.tracef(
					"Executing JDBC batch (%s / %s) - `%s`",
					batchPosition,
					batchSizeToUse,
					getKey().toLoggableString()
			);
		}

		//noinspection deprecation
		final JdbcObserver observer = jdbcCoordinator.getJdbcSessionOwner().getJdbcSessionContext().getObserver();
		try {
			getStatementGroup().forEachStatement( (tableName, statementDetails) -> {
				final String sql = statementDetails.getTableMutation().getSqlString();
				final PreparedStatement statement = statementDetails.getStatement();

				try {
					if ( statementDetails.getTableMutation().getPrimaryTableIndex() == 0 ) {
						final int[] rowCounts;
						try {
							observer.jdbcExecuteBatchStart();
							rowCounts = statement.executeBatch();
						}
						finally {
							observer.jdbcExecuteBatchEnd();
						}
						checkRowCounts( rowCounts, statementDetails );
					}
					else {
						statement.executeBatch();
					}
				}
				catch (SQLException e) {
					abortBatch( e );
					LOG.unableToExecuteBatch( e, sql );
					throw sqlExceptionHelper.convert( e, "could not execute batch", sql );
				}
				catch (RuntimeException re) {
					abortBatch( re );
					LOG.unableToExecuteBatch( re, sql );
					throw re;
				}
			} );
		}
		finally {
			batchPosition = 0;
		}
	}

	private void checkRowCounts(int[] rowCounts, PreparedStatementDetails statementDetails) throws SQLException, HibernateException {
		final int numberOfRowCounts = rowCounts.length;
		if ( batchPosition != 0 && numberOfRowCounts != batchPosition / getStatementGroup().getNumberOfStatements() ) {
			LOG.unexpectedRowCounts();
		}
		for ( int i = 0; i < numberOfRowCounts; i++ ) {
			statementDetails.getExpectation().verifyOutcome( rowCounts[i], statementDetails.getStatement(), i, statementDetails.getTableMutation().getSqlString() );
		}
	}

	@Override
	public void release() {
		if ( getStatementGroup().getNumberOfStatements() != 0 ) {
			LOG.batchContainedStatementsOnRelease();
		}
		releaseStatements();
		observers.clear();
	}

	@Override
	public String toString() {
		return "Batch2Impl(" + getKey().toLoggableString() + ")";
	}
}
