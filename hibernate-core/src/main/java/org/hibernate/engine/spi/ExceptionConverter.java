/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.engine.spi;

import javax.persistence.PersistenceException;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.LockOptions;

/**
 * @author Andrea Boriero
 */
public interface ExceptionConverter {
	/**
	 * Handles marking for rollback and other such operations that need to occur depending on the type of
	 * exception being handled.
	 *
	 * @param e The exception being handled.
	 */
	void handlePersistenceException(PersistenceException e);

	/**
	 * Delegates to {@link #handlePersistenceException} and then throws the given exception.
	 *
	 * @param e The exception being handled and finally thrown.
	 */
	void throwPersistenceException(PersistenceException e);

	/**
	 * Handle exceptions thrown during the transaction commit transaction phase
	 *
	 * @param e The exception being handled and finally thrown.
	 */
	void handleCommitException(RuntimeException e);

	/**
	 * Converts a Hibernate-specific exception into a JPA-specified exception; note that the JPA sepcification makes use
	 * of exceptions outside its exception hierarchy, though they are all runtime exceptions.
	 * <p/>
	 * Any appropriate/needed calls to {@link #handlePersistenceException} are also made.
	 *
	 * @param e The Hibernate excepton.
	 * @param lockOptions The lock options in effect at the time of exception (can be null)
	 *
	 * @return The JPA-specified exception
	 */
	RuntimeException convert(HibernateException e, LockOptions lockOptions);

	/**
	 * Converts a Hibernate-specific exception into a JPA-specified exception; note that the JPA sepcification makes use
	 * of exceptions outside its exception hierarchy, though they are all runtime exceptions.
	 * <p/>
	 * Any appropriate/needed calls to {@link #handlePersistenceException} are also made.
	 *
	 * @param e The Hibernate excepton.
	 *
	 * @return The JPA-specified exception
	 */
	RuntimeException convert(HibernateException e);

	RuntimeException convert(RuntimeException e);

	RuntimeException convert(RuntimeException e, LockOptions lockOptions);

	JDBCException convert(SQLException e, String message);
}
