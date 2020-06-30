/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results;

import org.hibernate.HibernateException;

/**
 * Exception for problems creating parts of the DomainResultAssembler
 * and Initializer trees
 *
 * @author Steve Ebersole
 */
public class AssemblerCreationException extends HibernateException {
	public AssemblerCreationException(String message) {
		super( message );
	}

	public AssemblerCreationException(String message, Throwable cause) {
		super( message, cause );
	}
}
