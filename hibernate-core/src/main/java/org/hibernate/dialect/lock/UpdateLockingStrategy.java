/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.dialect.lock;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.LockMode;
import org.hibernate.StaleObjectStateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.metamodel.model.domain.spi.Lockable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.sql.Update;
import org.hibernate.sql.exec.spi.BasicExecutionContext;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.spi.TypeConfiguration;

import org.jboss.logging.Logger;

/**
 * A locking strategy where the locks are obtained through update statements.
 * <p/>
 * This strategy is not valid for read style locks.
 *
 * @author Steve Ebersole
 * @since 3.2
 */
public class UpdateLockingStrategy implements LockingStrategy {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger(
			CoreMessageLogger.class,
			UpdateLockingStrategy.class.getName()
	);

	private final Lockable lockable;
	private final LockMode lockMode;
	private final String sql;

	/**
	 * Construct a locking strategy based on SQL UPDATE statements.
	 *
	 * @param lockable The metadata for the entity to be locked.
	 * @param lockMode Indictates the type of lock to be acquired.  Note that
	 * read-locks are not valid for this strategy.
	 */
	public UpdateLockingStrategy(Lockable lockable, LockMode lockMode) {
		this.lockable = lockable;
		this.lockMode = lockMode;
		if ( lockMode.lessThan( LockMode.UPGRADE ) ) {
			throw new HibernateException( "[" + lockMode + "] not valid for update statement" );
		}
		if ( StringHelper.isEmpty( lockable.getVersionColumnName() ) ) {
			LOG.writeLocksNotSupported( lockable.getEntityName() );
			this.sql = null;
		}
		else {
			this.sql = generateLockString();
		}
	}

	@Override
	public void lock(
			Serializable id,
			Object version,
			Object object,
			int timeout,
			SharedSessionContractImplementor session) throws StaleObjectStateException, JDBCException {
		if ( StringHelper.isEmpty( lockable.getVersionColumnName() ) ) {
			throw new HibernateException( "write locks via update not supported for non-versioned entities [" + lockable.getEntityName() + "]" );
		}

		final ExecutionContext executionContext = new BasicExecutionContext( session );
		final SessionFactoryImplementor factory = session.getFactory();
		final TypeConfiguration typeConfiguration = factory.getTypeConfiguration();

		// todo : should we additionally check the current isolation mode explicitly?

		try {
			final PreparedStatement st = session.getJdbcCoordinator().getStatementPreparer().prepareStatement( sql );
			try {
				final ValueBinder versionValueBinder = lockable.getHierarchy()
						.getVersionDescriptor()
						.getBasicType()
						.getValueBinder( typeConfiguration );
				versionValueBinder.bind( st, 1, version, executionContext );
				int offset = 2;

				final AllowableParameterType identifierParameterType = lockable.getHierarchy()
						.getIdentifierDescriptor();

				identifierParameterType.getValueBinder( typeConfiguration )
						.bind( st, offset, id, executionContext );

				offset += identifierParameterType.getNumberOfJdbcParametersNeeded();

				versionValueBinder.bind( st, offset, version, executionContext );

				final int affected = session.getJdbcCoordinator().getResultSetReturn().executeUpdate( st );
				if ( affected < 0 ) {
					if ( factory.getStatistics().isStatisticsEnabled() ) {
						factory.getStatistics().optimisticFailure( lockable.getEntityName() );
					}
					throw new StaleObjectStateException( lockable.getEntityName(), id );
				}

			}
			finally {
				session.getJdbcCoordinator().getLogicalConnection().getResourceRegistry().release( st );
				session.getJdbcCoordinator().afterStatementExecution();
			}

		}
		catch ( SQLException sqle ) {
			throw session.getJdbcServices().getSqlExceptionHelper().convert(
					sqle,
					"could not lock: " + MessageHelper.infoString( lockable, id, session.getFactory() ),
					sql
			);
		}
	}

	protected String generateLockString() {
		final SessionFactoryImplementor factory = lockable.getFactory();
		final Update update = new Update( factory.getDialect() );
		update.setTableName( lockable.getRootTableName() );
		update.addPrimaryKeyColumns( lockable.getRootTableIdentifierColumnNames() );
		update.setVersionColumnName( lockable.getVersionColumnName() );
		update.addColumn( lockable.getVersionColumnName() );
		if ( factory.getSessionFactoryOptions().isCommentsEnabled() ) {
			update.setComment( lockMode + " lock " + lockable.getEntityName() );
		}
		return update.toStatementString();
	}

	protected LockMode getLockMode() {
		return lockMode;
	}

}
