/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.junit5.envers;

import java.util.function.Consumer;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

import org.jboss.logging.Logger;

import org.hibernate.testing.junit5.SessionFactoryAccess;

/**
 * A scope or holder for the SessionFactory instance associated with a given test class.
 * Used to:
 * <ul>
 *     <li>Provide lifecycle management related to the SessionFactory</li>
 * </ul>
 *
 * @author Chris Cranford
 */
public class EnversSessionFactoryScope implements SessionFactoryAccess {
	private static final Logger log = Logger.getLogger( EnversSessionFactoryScope.class );

	private final EnversSessionFactoryProducer sessionFactoryProducer;
	private final Strategy auditStrategy;

	private SessionFactory sessionFactory;

	public EnversSessionFactoryScope(EnversSessionFactoryProducer producer, Strategy auditStrategy) {
		log.debugf( "#<init> - %s", auditStrategy.getDisplayName() );
		this.auditStrategy = auditStrategy;
		this.sessionFactoryProducer = producer;
	}

	public void releaseSessionFactory() {
		log.debugf( "#releaseSessionFactory - %s", auditStrategy.getDisplayName() );
		if ( sessionFactory != null ) {
			log.infof( "Closing SessionFactory %s (%s)", sessionFactory, auditStrategy.getDisplayName() );
			sessionFactory.close();
			sessionFactory = null;
		}
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		log.debugf( "#getSessionFactory - %s", auditStrategy.getDisplayName() );
		if ( sessionFactory == null || sessionFactory.isClosed() ) {
			sessionFactory = sessionFactoryProducer.produceSessionFactory( auditStrategy.getSettingValue() );
		}
		return (SessionFactoryImplementor) sessionFactory;
	}

	public void inSession(Consumer<SessionImplementor> action) {
		log.trace( "#inSession(action)" );
		inSession( getSessionFactory(), action );
	}

	public void inTransaction(Consumer<SessionImplementor> action) {
		log.trace( "#inTransaction(action)" );
		inTransaction( getSessionFactory(), action );
	}

	public void inSession(SessionFactoryImplementor sfi, Consumer<SessionImplementor> action) {
		log.trace( "##inSession(SF,action)" );
		try ( SessionImplementor session = (SessionImplementor) sfi.openSession() ) {
			log.trace( "Session opened, calling action" );
			action.accept( session );
			log.trace( "called action" );
		}
		finally {
			log.trace( "Session close - auto-close lock" );
		}
	}

	public void inTransaction(SessionFactoryImplementor sfi, Consumer<SessionImplementor> action) {
		log.trace( "#inTransaction(SF,action)" );
		try ( SessionImplementor session = (SessionImplementor) sfi.openSession() ) {
			log.trace( "Session opened, calling action" );
			inTransaction( session, action );
			log.trace( "called action" );
		}
		finally {
			log.trace( "Session close - auto-close lock" );
		}
	}

	public void inTransaction(SessionImplementor session, Consumer<SessionImplementor> action) {
		log.trace( "#inTransaction(session,action)" );

		final Transaction trx = session.beginTransaction();
		try {
			log.trace( "Transaction started, calling action." );
			action.accept( session );
			log.trace( "Action called, attempting to commit transaction." );
			trx.commit();
			log.trace( "Commit successful." );
		}
		catch ( Exception e ) {
			log.tracef( "Error calling action: %s (%s) - rolling back", e.getClass().getName(), e.getMessage() );
			try {
				trx.rollback();
			}
			catch ( Exception ignored ) {
				log.trace( "Was unable to rollback transaction." );
			}
			throw e;
		}
	}

	public void inAuditReader(Consumer<AuditReader> action) {
		log.trace( "#inAuditReader" );
		inAuditReader( getSessionFactory(), action );
	}

	public void inAuditReader(SessionFactoryImplementor sfi, Consumer<AuditReader> action) {
		try ( SessionImplementor session = (SessionImplementor) sfi.openSession() ) {
			AuditReader auditReader = AuditReaderFactory.get( session );
			action.accept( auditReader );
		}
	}
}
